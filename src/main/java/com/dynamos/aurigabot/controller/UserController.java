package com.dynamos.aurigabot.controller;

import com.dynamos.aurigabot.dto.UserDto;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.response.HttpApiResponse;
import com.dynamos.aurigabot.service.CustomUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private CustomUserService customUserService;

    @PostMapping(value = "/add")
    public Mono<HttpApiResponse> addNewUser(@RequestBody UserDto userDto){
//        customUserService.validateUserDetails(userDto);

        return null;
    }
}
