package com.dynamos.aurigabot.entity.converters;

import com.dynamos.aurigabot.entity.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@WritingConverter
public class UserWriteConverter implements Converter<User, OutboundRow> {

    @Override
    public OutboundRow convert(User user) {
        OutboundRow row = new OutboundRow();
        if(user.getId() != null) {
            row.put("id", Parameter.from(user.getId()));
        } else {
            row.put("id", Parameter.from(UUID.randomUUID()));
        }

        java.util.Date utilDob = null;
        if(user.getDob() != null) {
            Date sqlDob = user.getDob();
            utilDob = new java.util.Date(sqlDob.getTime());
        }

        row.put("name", Parameter.fromOrEmpty(user.getName(), String.class));
        row.put("email", Parameter.fromOrEmpty(user.getEmail(), String.class));
        row.put("mobile", Parameter.fromOrEmpty(user.getMobile(), String.class));
        row.put("password", Parameter.fromOrEmpty(user.getPassword(), String.class));
        row.put("username", Parameter.fromOrEmpty(user.getUsername(), String.class));
        row.put("role", Parameter.fromOrEmpty(user.getRole(), String.class));
        row.put("employee_id", Parameter.fromOrEmpty(user.getEmployeeId(), UUID.class));
        row.put("telegram_chat_id", Parameter.fromOrEmpty(user.getTelegramChatId(), String.class));
        row.put("dob", Parameter.fromOrEmpty(utilDob, java.util.Date.class));

        return row;
    }
}