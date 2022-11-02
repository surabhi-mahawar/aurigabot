package com.dynamos.aurigabot.service;

import com.dynamos.aurigabot.dto.UserDto;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.enums.RoleType;
import com.dynamos.aurigabot.repository.UserRepository;
import com.dynamos.aurigabot.response.HttpApiResponse;
import com.dynamos.aurigabot.utils.BotUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


import java.security.InvalidParameterException;
import java.util.UUID;

@Service
public class CustomUserService {

     @Autowired
     private UserRepository userRepository;
     @Autowired
     private PasswordEncoder passwordEncoder;

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
                 .password(userDto.getPassword())
                 .role(userDto.getRole())
                 .build();

         ObjectMapper objectMapper = new ObjectMapper();
         ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();

         return user;
     }
     public User addSuperAdmin(){
         System.out.println("Called");
         User existingUser = userRepository.findById(BotUtil.USER_ADMIN_ID).block();
         if(existingUser == null) {
             System.out.println("Inserting Superadmin User.");

             UserDto admin = UserDto.builder().build();
             admin.setId(BotUtil.USER_ADMIN_ID);
             admin.setName("superadmin");
             admin.setMobile("9876543210");
             admin.setEmail("superadmin@aurigait.com");
             admin.setUsername("superadmin");
//             admin.setEmployeeId(101);
             admin.setPassword(passwordEncoder.encode("password"));
             admin.setRole(RoleType.ADMIN.name());
             admin.setEmployeeId(UUID.randomUUID());

             return userRepository.save(convertUserDtoToDao(admin)).block();
         } else {
             System.out.println("Superadmin User already exists.");
             return existingUser;
         }
     }

    public Mono<HttpApiResponse> validateUserDetails(UserDto userDto){
         String username = userDto.getUsername();
         String email = userDto.getEmail();
         String mobile = userDto.getMobile();
         String name = userDto.getName();

         String emailRegex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$";
         String mobileRegex = "(0/91)?[7-9][0-9]{9}";

//         if (username.isEmpty()){
//             return HttpApiResponse.builder()
//                     .status(HttpStatus.SC_LENGTH_REQUIRED)
//                     .error("Invalid username")
//                     .message("Please input valid username")
//                     .build();
//         }
        return null;
    }

    public Mono<User> addNewUser(UserDto userDto){
      return userRepository.save(convertUserDtoToDao(userDto));

    }
}
