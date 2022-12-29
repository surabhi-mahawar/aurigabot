package com.aurigabot.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Builder
@Data
@Table( name = "employee_manager")
public class EmployeeManager {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private User employee;

   @OneToMany(fetch = FetchType.LAZY)
   @JoinColumn(name = "manager_id")
   private User manager;
}
