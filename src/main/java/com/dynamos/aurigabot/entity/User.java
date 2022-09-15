package com.dynamos.aurigabot.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Builder
@Data
@Table(name = "users")
public class User {
    @Id
    private UUID id;
    private String name;
    private String mobile;
    private String email;
    private String username;
    private Integer employeeId;

}
