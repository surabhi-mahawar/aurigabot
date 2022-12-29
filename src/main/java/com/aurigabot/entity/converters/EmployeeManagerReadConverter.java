package com.aurigabot.entity.converters;

import com.aurigabot.entity.EmployeeManager;
import com.aurigabot.entity.LeaveRequest;
import com.aurigabot.entity.User;
import com.aurigabot.enums.LeaveStatus;
import com.aurigabot.enums.LeaveType;
import com.aurigabot.enums.*;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDate;
import java.util.UUID;

@ReadingConverter
public class EmployeeManagerReadConverter implements Converter<Row, EmployeeManager> {

    @Override
    public EmployeeManager convert(Row source) {
        User manager;
        User employee;
        if(source.get("employee_id", UUID.class) != null) {
            manager = User.builder().id(source.get("u_id", UUID.class))
                    .name(source.get("u_name",String.class))
//                    .dob(source.get("u_dob", Date.class))
                    .email(source.get("u_email", String.class))
                    .mobile(source.get("u_mobile", String.class))
                    .password(source.get("u_password", String.class))
                    .employeeId(source.get("u_employee_id", UUID.class))
                    .role(source.get("u_role",String.class))
                    .telegramChatId(source.get("u_telegram_chat_id",String.class))
                    .username(source.get("u_username",String.class))
                    .build();
            employee = User.builder().id(source.get("id", UUID.class))
                    .name(source.get("name",String.class))
//                    .dob(source.get("u_dob", Date.class))
                    .email(source.get("email", String.class))
                    .mobile(source.get("mobile", String.class))
                    .password(source.get("password", String.class))
                    .employeeId(source.get("employee_id", UUID.class))
                    .role(source.get("role",String.class))
                    .telegramChatId(source.get("telegram_chat_id",String.class))
                    .username(source.get("username",String.class))
                    .build();
        } else {
            manager = null;
            employee = null;
        }

        return EmployeeManager.builder()
                .id(source.get("id", UUID.class))
                .manager(manager)
                .employee(employee)
                .build();
    }
}