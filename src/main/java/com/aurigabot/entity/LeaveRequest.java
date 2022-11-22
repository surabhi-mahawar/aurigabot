package com.aurigabot.entity;

import com.aurigabot.enums.LeaveStatus;
import com.aurigabot.enums.LeaveType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.time.LocalDate;
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
