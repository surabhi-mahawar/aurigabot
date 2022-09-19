package com.dynamos.aurigabot.dto;

import com.dynamos.aurigabot.enums.MessagePayloadType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Builder
public class MessagePayloadDto {
    private String message;
    private MessagePayloadType msgType;
    private String media_url;
    private ArrayList<MessagePayloadChoiceDto> choices;

}
