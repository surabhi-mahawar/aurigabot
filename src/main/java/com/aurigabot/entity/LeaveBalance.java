package com.aurigabot.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Builder
@Data
@Table (name = "leave_balance")
public class LeaveBalance {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private User user;

    private int cl;
    private int pl;

}
