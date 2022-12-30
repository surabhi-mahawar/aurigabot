package com.aurigabot.service.calendar_events;


import com.aurigabot.entity.GoogleTokens;
import com.aurigabot.entity.User;
import com.aurigabot.repository.CalendarUserRepository;
import com.aurigabot.repository.UserRepository;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.services.oauth2.model.Userinfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
public class CalendarUserService {

    @Autowired
    private CalendarUserRepository calendarUserRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Save user information in DB
     * @param tokenResponse
     * @param userInfo
     * @return
     */
    public Mono<GoogleTokens> registerUser(TokenResponse tokenResponse, Userinfo userInfo) {
        try {
            Mono<GoogleTokens> monoGoogleTokens  = calendarUserRepository.findByEmail(userInfo.getEmail());
            Mono<User> monoUser = userRepository.findByEmail(userInfo.getEmail());

            return monoUser.map(user -> {
                GoogleTokens newGoogleToken= GoogleTokens.builder().build();
                newGoogleToken.setAccessToken(tokenResponse.getAccessToken());
                newGoogleToken.setRefreshToken(tokenResponse.getRefreshToken());
                newGoogleToken.setEmail(userInfo.getEmail());
                newGoogleToken.setName(userInfo.getName());
                newGoogleToken.setPhoto(userInfo.getPicture());
                newGoogleToken.setEmployeeId(user.getId());

                return monoGoogleTokens
                        .switchIfEmpty(calendarUserRepository.save(newGoogleToken))
                        .map(googleTokens ->{
                            googleTokens.setAccessToken(newGoogleToken.getAccessToken());
                            googleTokens.setRefreshToken(newGoogleToken.getRefreshToken());
                            googleTokens.setEmail(newGoogleToken.getEmail());
                            googleTokens.setName(newGoogleToken.getName());
                            googleTokens.setPhoto(newGoogleToken.getPhoto());
                            return calendarUserRepository.save(googleTokens);
                        }).flatMap(new Function<Mono<GoogleTokens>, Mono<? extends GoogleTokens>>() {
                            @Override
                            public Mono<? extends GoogleTokens> apply(Mono<GoogleTokens> monoMono) {
                                return monoMono;
                            }
                        });

            })
                    .flatMap(new Function<Mono<GoogleTokens>, Mono<? extends GoogleTokens>>() {
                        @Override
                        public Mono<? extends GoogleTokens> apply(Mono<GoogleTokens> monoMono) {
                            return monoMono;
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //TODO: Change the getCurrentUser() Query.
    public Mono<GoogleTokens> getCurrentUser() {
        try {
            return calendarUserRepository.findTopByOrderByIdDesc();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Mono<GoogleTokens> getCurrentUser(String email) {
        try {
            return calendarUserRepository.findByEmail(email);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Mono<GoogleTokens> saveUser(GoogleTokens user){
        return calendarUserRepository.save(user);
    }

}
