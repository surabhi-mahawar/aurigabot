package com.dynamos.aurigabot.controller;

import com.dynamos.aurigabot.dao.UserDao;
import com.dynamos.aurigabot.entity.AMessage;
import com.dynamos.aurigabot.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
public class BirthdaysController {

    @Autowired
    UserDao userDao;

    @GetMapping("/api/v1/birthdays")
    public JsonNode birthday() throws JsonProcessingException {
        List<User> users = userDao.getUserByDate();
        AMessage message = new AMessage();
        String str = "";
        for(User user : users){
            str += "Happy birthday! " + user.getName() + "\n";
        }
        message.setOptions("");
        message.setText(str);
        message.setSentAt(new Date());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(message);
        System.out.println(jsonString);
        return objectMapper.readTree(jsonString);
    }
}
