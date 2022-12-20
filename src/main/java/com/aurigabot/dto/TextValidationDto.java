package com.aurigabot.dto;

import com.aurigabot.enums.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TextValidationDto {
    private FieldType fieldType;
    private Integer length;
    private String regex;
    private Integer min;
    private Integer max;

}
