package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.LeaveStatus;
import lombok.Builder;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;
import java.util.UUID;

@Builder
@Data
@Table( name = "leave_request")
public class LeaveRequest {
    @Id
    private UUID id;
    private Integer employeeId;
    private String reason;
    private Date fromDate;
    private Date toDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    private String approvedBy;

}
