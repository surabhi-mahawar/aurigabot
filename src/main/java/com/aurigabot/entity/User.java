package com.aurigabot.entity;

import com.aurigabot.utils.BotUtil;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.*;
import java.sql.Date;
import java.util.UUID;

@Builder
@Data
@Table(name = "users")
public class User implements Persistable {
    @Id
    private UUID id;
    private String password;
    private String name;
    private String mobile;
    private String email;
    private String username;
    private Date dob;
    private String role;
    private UUID employeeId;
    private UUID managerId;
    private String telegramChatId;

    @Override
    @Transient
    public boolean isNew() {
        /** Check for superadmin user id
         * Reason - To insert superadmin user on application run with a specific id
         */
        if(id.equals(BotUtil.USER_ADMIN_ID)) {
            return true;
        }
        return id == null;
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
