package com.dynamos.aurigabot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessagePayloadChoiceDto {
    private String key;
    private String text;
}
