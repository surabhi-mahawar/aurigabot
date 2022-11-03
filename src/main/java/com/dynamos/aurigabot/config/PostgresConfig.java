package com.dynamos.aurigabot.config;

import com.dynamos.aurigabot.entity.converters.FlowReadConverter;
import com.dynamos.aurigabot.entity.converters.UserMessageReadConverter;
import com.dynamos.aurigabot.entity.converters.UserMessageWriteConverter;
import com.dynamos.aurigabot.entity.converters.UserWriteConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

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
        converters.add(new UserWriteConverter());
        R2dbcDialect dialect = DialectResolver.getDialect(connectionFactory);
        return R2dbcCustomConversions.of(dialect, converters);
    }
}
