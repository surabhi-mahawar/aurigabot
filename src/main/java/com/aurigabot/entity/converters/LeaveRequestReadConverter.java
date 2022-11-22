package com.aurigabot.entity.converters;

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
public class LeaveRequestReadConverter implements Converter<Row, LeaveRequest> {

    @Override
    public LeaveRequest convert(Row source) {
        User user;
        if(source.get("employee_id", UUID.class) != null) {
            user = User.builder().id(source.get("u_id", UUID.class))
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
        } else {
            user = null;
        }

        return LeaveRequest.builder()
                .id(source.get("id", UUID.class))
                .employeeId(user)
                .toDate(source.get("to_date", LocalDate.class))
                .fromDate(source.get("from_date", LocalDate.class))
                .reason(source.get("reason", String.class))
                .status(LeaveStatus.valueOf(source.get("status", String.class)))
                .leaveType(LeaveType.getEnumByValue(source.get("leave_type", String.class)))
                .approvedBy(source.get("approved_by", UUID.class))
                .build();
    }
}