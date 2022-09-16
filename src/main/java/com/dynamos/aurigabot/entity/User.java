package com.dynamos.aurigabot.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.FetchType;
import javax.persistence.OneToOne;
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

    private UUID employeeId;


    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user")
    private LeaveRequest leaveRequest;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user")
    private LeaveBalance leaveBalance;

    @OneToOne(fetch = FetchType.LAZY,mappedBy = "user")
    private EmployeeManager employeeManager;

}
