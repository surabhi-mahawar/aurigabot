package com.aurigabot.dto;

import com.aurigabot.enums.FieldType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationDto {
    private FieldType fieldType;

    private DateValidationDto dateValidationConfig;

    private DateTimeValidationDto dateTimeValidationConfig;

    private TextValidationDto textValidationConfig;

}
