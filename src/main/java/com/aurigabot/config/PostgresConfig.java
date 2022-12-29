package com.aurigabot.config;

import com.aurigabot.entity.converters.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;

import java.util.ArrayList;
import java.util.List;

@Configuration
//@EnableR2dbcRepositories
public class PostgresConfig {
    /**
     * Add custom converters for R2dbc queries
     * @param connectionFactory
     * @param objectMapper
     * @return
     */
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        List<Converter<?,?>> converters = new ArrayList<>();
        converters.add(new UserMessageWriteConverter());
        converters.add(new UserMessageReadConverter());
        converters.add(new FlowReadConverter());
//        converters.add(new UserReadConverter());
        converters.add(new UserWriteConverter());
        converters.add(new LeaveRequestReadConverter());
        converters.add(new LeaveRequestWriteConverter());
        converters.add(new EmployeeManagerReadConverter());
        R2dbcDialect dialect = DialectResolver.getDialect(connectionFactory);
        return R2dbcCustomConversions.of(dialect, converters);
    }
}
