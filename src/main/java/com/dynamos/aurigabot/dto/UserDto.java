package com.dynamos.aurigabot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserDto {
    private UUID id;
    private String name;
    private String mobile;
    private String email;
    private String username;
    private Date dob;
    private int employeeId;
}
