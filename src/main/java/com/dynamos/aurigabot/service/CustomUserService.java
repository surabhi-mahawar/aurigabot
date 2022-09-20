package com.dynamos.aurigabot.service;

import com.dynamos.aurigabot.dto.UserDto;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class CustomUserService {

     @Autowired
     private UserRepository userRepository;

     public static User convertUserDtoToDao(UserDto userDto){
         System.out.println("Method called");
         User user = User.builder()
                 .id(userDto.getId())
                 .name(userDto.getName())
                 .mobile(userDto.getMobile())
                 .dob(userDto.getDob())
                 .username(userDto.getUsername())
                 .employeeId(userDto.getEmployeeId())
                 .email(userDto.getEmail())
                 .build();

         ObjectMapper objectMapper = new ObjectMapper();
         ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();

         return user;
     }
     public User addSuperAdmin(){
         System.out.println("Called");
         UserDto admin = UserDto.builder().build();
         admin.setId(UUID.fromString("89326ca8-f4cf-4756-b180-8636824345bd"));
         admin.setName("superadmin");
         admin.setMobile("9876543210");
         admin.setEmail("superadmin@aurigait.com");
         admin.setUsername("superadmin");
         admin.setEmployeeId(101);

         return userRepository.save(convertUserDtoToDao(admin)).block();

    }

    public Mono<User> addNewUser(UserDto userDto){
      return userRepository.save(convertUserDtoToDao(userDto));

    }
}
