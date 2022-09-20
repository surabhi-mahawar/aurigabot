package com.dynamos.aurigabot.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@Table(name = "users")
public class User implements Persistable {
    @Id
    private UUID id;
    private String name;
    private String mobile;
    private String email;
    private String username;
    private Date dob;
    private int employeeId;



    @Override
    @Transient
    public boolean isNew() {
        return id == null  ;
    }





//    @OneToOne(fetch = FetchType.LAZY, mappedBy = "users")
//    private LeaveRequest leaveRequest;
//
//    @OneToOne(fetch = FetchType.LAZY, mappedBy = "users")
//    private LeaveBalance leaveBalance;
//
//    @OneToOne(fetch = FetchType.LAZY,mappedBy = "users")
//    private EmployeeManager employee;
//
//    @OneToMany(fetch = FetchType.LAZY , mappedBy = "users")
//    private EmployeeManager manager;

}
