package com.aurigabot.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowPayloadDto {
    private String question;
    private String questionType;
    private List<FlowPayloadChoiceDto> choices;

    private ValidationDto validationDto;
    private String mediaUrl;
}
