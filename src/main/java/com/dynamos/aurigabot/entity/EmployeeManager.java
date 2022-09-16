package com.dynamos.aurigabot.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Builder
@Data
@Table( name = "table_manager")
public class EmployeeManager {
    @Id
    private UUID id;
    private Integer employeeId;
    private Integer managerId;
}
