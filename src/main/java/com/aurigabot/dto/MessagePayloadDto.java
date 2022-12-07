package com.aurigabot.dto;

import com.aurigabot.enums.MessagePayloadType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePayloadDto {
    private String message;
    private MessagePayloadType msgType;
    private String mediaUrl;
    private ArrayList<MessagePayloadChoiceDto> choices;
}
