package com.dynamos.aurigabot.controller;

import com.dynamos.aurigabot.config.security.UserDetailsSecurity;
import com.dynamos.aurigabot.dto.UserDto;
import com.dynamos.aurigabot.repository.UserRepository;
import com.dynamos.aurigabot.response.HttpApiResponse;
import com.dynamos.aurigabot.service.CustomUserService;
import com.dynamos.aurigabot.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/admin")
public class AdminController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserService customUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping(value = "/login")
    public HttpApiResponse login(@RequestBody UserDto userDto){
        System.out.println("Hello Admin"+userDto);
//        customUserService.validateUserDetails(userDto);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getUsername(),userDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsSecurity userDetailsSecurity = (UserDetailsSecurity)authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetailsSecurity);
        System.out.println("JWT:"+jwt);
        List<String> roles = userDetailsSecurity.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return HttpApiResponse.builder()
                .message("Token : "+jwt)
                .status(HttpStatus.OK.value())
                .build();




    }
}
