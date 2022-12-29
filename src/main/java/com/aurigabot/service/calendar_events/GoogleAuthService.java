package com.aurigabot.service.calendar_events;

import com.aurigabot.entity.GoogleTokens;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.api.services.oauth2.model.Userinfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class GoogleAuthService {

    @Autowired
    private CalendarUserService calendarUserService;

    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private GoogleClientSecrets clientSecrets;
    private GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow;
    private Credential credential;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectURI;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    /**
     * Open google login page
     * @return
     * @throws Exception
     */
    public String authorize() throws Exception {
        Collection<String> SCOPES = new ArrayList<String>();
        SCOPES.add(CalendarScopes.CALENDAR);
        SCOPES.add(Oauth2Scopes.USERINFO_EMAIL);
        SCOPES.add(Oauth2Scopes.USERINFO_PROFILE);

        if (googleAuthorizationCodeFlow == null) {
            GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
            web.setClientId(clientId);
            web.setClientSecret(clientSecret);
            clientSecrets = new GoogleClientSecrets().setWeb(web);
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
                    SCOPES).setAccessType("offline").setApprovalPrompt("force").build();
        }
        AuthorizationCodeRequestUrl authorizationUrl = googleAuthorizationCodeFlow.newAuthorizationUrl().setRedirectUri(redirectURI);
        return authorizationUrl.build();
    }

    /**
     * To extract access token and user info after signin
     * @param code
     * @return
     */
    public String extractAccessToken(String code) {
        try {
            TokenResponse response = googleAuthorizationCodeFlow.newTokenRequest(code).setRedirectUri(redirectURI).execute();
            credential = googleAuthorizationCodeFlow.createAndStoreCredential(response, "userID");
            Oauth2 oauth2Client =
                    new Oauth2.Builder(httpTransport, JSON_FACTORY, credential)
                            .setApplicationName("Oauth")
                            .build();

            Userinfo userInfo = oauth2Client.userinfo().get().execute();
            Mono<GoogleTokens> result = calendarUserService.registerUser(response, userInfo);
            result.subscribe();
            return "Token saved please head back to telegram bot.";
        } catch (Exception e) {
            System.out.println(e);
            return "Couldn't save token please try again.";
        }
    }

    /**
     * Generate new access token from the refresh token saved in DB
     * @return
     */
    public Mono<String> getNewAccessTokenUsingRefreshToken() {
            Mono<GoogleTokens> monoUser = calendarUserService.getCurrentUser();
            return monoUser.map(user -> {
                String newToken = null;
                try {
                    newToken = getNewAccessToken(user.getRefreshToken(),clientId, clientSecret);
                } catch (Exception e) {
                    return "Error encountered while updating access token";
                }
                user.setAccessToken(newToken);
            calendarUserService.saveUser(user).subscribe();
            return "Access token updated";
            });
    }

    public String getNewAccessToken(String refreshToken, String clientId, String clientSecret) throws IOException {
        ArrayList<String> SCOPES = new ArrayList<>();

        SCOPES.add(CalendarScopes.CALENDAR);
        SCOPES.add(Oauth2Scopes.USERINFO_EMAIL);
        SCOPES.add(Oauth2Scopes.USERINFO_PROFILE);

        TokenResponse tokenResponse = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                refreshToken, clientId, clientSecret).setScopes(SCOPES).setGrantType("refresh_token").execute();

        return tokenResponse.getAccessToken();
    }
}
