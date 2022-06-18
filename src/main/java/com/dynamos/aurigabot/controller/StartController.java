package com.dynamos.aurigabot.controller;

import com.dynamos.aurigabot.dao.AMessageDao;
import com.dynamos.aurigabot.dao.UserDao;
import com.dynamos.aurigabot.entity.AMessage;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.enums.State;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static java.util.Objects.isNull;

@RestController
public class StartController {

    @Autowired
    UserDao userDao;

    @Autowired
    AMessageDao messageDao;

    @PostMapping("/api/v1/test")
    public String testAPICall(@RequestBody JsonNode json) throws JsonProcessingException {
        return "test";
    }

    @PostMapping("/api/v1/register")
    public void register(@RequestBody JsonNode jsonNode) throws ParseException {
        System.out.println(jsonNode);

        User user = new User();
        user.setName(jsonNode.get("name").asText());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = dateFormat.parse(jsonNode.get("dob").asText());
        user.setDob(date);

        date = dateFormat.parse(jsonNode.get("joinedAt").asText());
        user.setJoinedAt(date);

        user.setEmail(jsonNode.get("email").asText());
//        user.setjsonNode.get("phone");
        user.setInterfaceUserId(jsonNode.get("interfaceUserId").asText());

        userDao.save(user);
    }

    @PostMapping("/api/v1/start")
    public String startAPICall(@RequestBody JsonNode json){
        System.out.println(json);
        String key = json.get("interfaceUserId").asText();
        User user = userDao.findByInterfaceUserId (key);
        if (isNull(user)) {
            return "";
        }
        else return user.getName();
    }



}
