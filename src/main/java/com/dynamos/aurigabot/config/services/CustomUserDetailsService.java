package com.dynamos.aurigabot.config.services;

import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.transaction.Transactional;

public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findFirstByEmail(username).block();
        if (user == null ) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        return customUserDetails;


    }
}
