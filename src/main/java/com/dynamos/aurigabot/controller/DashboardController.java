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

import java.util.Date;

@RestController
public class DashboardController {

    @Autowired
    UserDao userDao;

    @Autowired
    AMessageDao messageDao;

    @GetMapping("api/v1/dashboard")
    public JsonNode getDashboard(@RequestBody JsonNode userDetails) throws JsonProcessingException {

        User user = userDao.findByInterfaceUserId(userDetails.get("interfaceUserId").asText());
        ObjectMapper objectMapper = new ObjectMapper();

        AMessage msg=objectMapper.readValue(userDetails.toString(),AMessage.class);
        messageDao.save(msg);
        msg.setText("Hello! " + user.getName() +"\n"
                +"hope you are doing well."
        );
        msg.setReceivedAt(null);
        msg.setSentAt(new Date());
        msg.setState(State.REPLIED);
        msg.setUser(user);

//        String json = "{ \"text\":\"Hello "+userDetails.get("text").asText()+"\",\"receivedAt\":\""+userDetails.get("receivedAt").asText()+"\",\"sentAt\":null, \"state\":\""+ State.REPLIED+"\", \"user\":\""+userDetails.get("user").asText()+"\", \"command\":\""+userDetails.get("command").asText()+"\", \"botName\":\""+userDetails.get("botName").asText()+"\" }";

        String test=objectMapper.writeValueAsString(msg);
        JsonNode result=objectMapper.readTree(test);
        messageDao.save(msg);
        return result;
    }
    
    
}
