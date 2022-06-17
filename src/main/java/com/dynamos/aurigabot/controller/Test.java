package com.dynamos.aurigabot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
public class Test {

    @PostMapping("/api/v1/test")
    public String testAPICall(@RequestBody JsonNode json){
        System.out.println(json);
        return "test";
    }

}
