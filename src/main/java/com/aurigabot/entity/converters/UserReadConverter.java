package com.aurigabot.entity.converters;

import com.aurigabot.entity.User;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.sql.Date;
import java.util.UUID;

/**
 * User read converter
 */
@ReadingConverter
public class UserReadConverter implements Converter<Row, User> {

    @Override
    public User convert(Row source) {
        return User.builder().id(source.get("id", UUID.class))
                .name(source.get("name",String.class))
                .dob(source.get("dob", Date.class))
                .email(source.get("email", String.class))
                .mobile(source.get("mobile", String.class))
                .password(source.get("password", String.class))
                .employeeId(source.get("employee_id", UUID.class))
                .role(source.get("role",String.class))
                .telegramChatId(source.get("telegram_chat_id",String.class))
                .username(source.get("username",String.class))
                .build();
    }
}
