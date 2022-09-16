package com.dynamos.aurigabot.repository;


import com.dynamos.aurigabot.entity.UserMessage;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMessageRepository extends R2dbcRepository<UserMessage,Long> {
}
