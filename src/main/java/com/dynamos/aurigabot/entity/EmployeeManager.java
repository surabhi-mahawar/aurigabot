package com.dynamos.aurigabot.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@Table( name = "table_manager")
public class EmployeeManager {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private User employee;

   @OneToMany(fetch = FetchType.LAZY)
   @JoinColumn(name = "manager_id")
   private User manager;

    private Integer managerId;
}
