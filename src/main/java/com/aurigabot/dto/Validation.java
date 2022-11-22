package com.aurigabot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Validation {
    private String format;
    private Integer length;
    private String regex;
    private String type;

}
