package com.dynamos.aurigabot.config.security;

import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserDetailsSecurityService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Email:"+username);
        User user = userRepository.findFirstByEmail(username).block();
        if (user == null ) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }

        UserDetailsSecurity userDetailsSecurity = new UserDetailsSecurity(user);
        return userDetailsSecurity;


    }
}
