package com.aurigabot.entity;

import com.aurigabot.dto.MessagePayloadChoiceDto;
import com.aurigabot.enums.MessagePayloadType;

import java.util.ArrayList;

public class UserMessagePayload {
    private String message;
    private MessagePayloadType msgType;
    private String media_url;
    private ArrayList<UserMessagePayloadChoice> choices;
}
