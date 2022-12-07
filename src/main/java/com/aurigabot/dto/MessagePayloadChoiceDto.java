package com.aurigabot.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePayloadChoiceDto {
    private String key;
    private String text;
}
