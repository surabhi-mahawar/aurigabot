package com.dynamos.aurigabot.dto;

import com.dynamos.aurigabot.enums.MessagePayloadType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Builder
public class Validation {
    private String format;
    private Integer length;
    private String regex;
    private String type;

}
