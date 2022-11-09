package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.LeaveStatus;
import com.dynamos.aurigabot.enums.LeaveType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@Table( name = "leave_request")
public class LeaveRequest implements Persistable{
    @Id
    @org.springframework.data.annotation.Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private User employeeId;

    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    private String reason;
    private LocalDate fromDate;
    private LocalDate toDate;
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;


    private UUID approvedBy;

    @Override
    public boolean isNew() {
        return id==null;
    }
}
