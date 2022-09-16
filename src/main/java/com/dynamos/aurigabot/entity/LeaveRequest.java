package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.LeaveStatus;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.sql.Date;
import java.util.UUID;

@Builder
@Data
@Table( name = "leave_request")
public class LeaveRequest {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private User user;

    private String reason;
    private Date fromDate;
    private Date toDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;


    private UUID approvedBy;

}
