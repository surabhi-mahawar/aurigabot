package com.dynamos.aurigabot.controller;

import com.dynamos.aurigabot.dao.UserDao;
import com.dynamos.aurigabot.entity.AMessage;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.enums.RoleType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.DataInput;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
public class BotControllers {

    @Autowired
    UserDao userDao;

    @PostMapping("/api/v1/test")
    public String testAPICall(@RequestBody JsonNode json) throws JsonProcessingException {

        return "test";
    }

    @GetMapping("/api/v1/birthdays")
    public JsonNode birthday() throws JsonProcessingException {
        List<User> users = userDao.getUserByDate();
        AMessage message = new AMessage();
        String str = "";
        for(User user : users){
            str += user.getName() + "\n";
        }
        message.setText(str);
        message.setSentAt(new Date());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(message);
        return objectMapper.readTree(jsonString);
    }

    @PostMapping("/api/v1/register")
    public void register(@RequestBody JsonNode jsonNode) throws ParseException {
        User user = new User();
        user.setName(jsonNode.get("name").toString());

        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date date = dateFormat.parse(jsonNode.get("dob").toString());
        user.setDob(date);

        date = dateFormat.parse(jsonNode.get("joinedAt").toString());
        user.setJoinedAt(date);

        user.setEmail(jsonNode.get("email").toString());
//        user.setjsonNode.get("phone");
        user.setInterfaceUserId(jsonNode.get("interfaceUserId").toString());

        userDao.save(user);
    }

}
