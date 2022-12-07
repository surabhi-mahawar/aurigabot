package com.aurigabot.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowPayloadChoiceDto {
    private String key;
    private String text;
}
