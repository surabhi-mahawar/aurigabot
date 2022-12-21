package com.aurigabot.dto;

import com.aurigabot.enums.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DateValidationDto {

    private String format;

    private String lte;
    private String gte;



}
