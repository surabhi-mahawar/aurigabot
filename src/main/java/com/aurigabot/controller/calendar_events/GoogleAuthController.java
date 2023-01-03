package com.aurigabot.controller.calendar_events;

import com.aurigabot.service.calendar_events.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;

@RestController
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @RequestMapping(value = "/oauth", method = RequestMethod.GET)
    public RedirectView googleConnectionStatus(HttpServletRequest request) throws Exception {
        return new RedirectView(googleAuthService.authorize());
    }

    @RequestMapping(value = "/oauth", method = RequestMethod.GET, params = "code")
    public String oauth2Callback(@RequestParam(value = "code") String code) {
        return googleAuthService.extractAccessToken(code);
    }
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    public ResponseEntity<String> refreshToken() {
        String email="vishal.bothra@aurigait.com";
        Mono<String> response = googleAuthService.getNewAccessTokenUsingRefreshToken(email);
        response.subscribe();
        return new ResponseEntity<>("Refreshed", HttpStatus.OK);
    }
}
