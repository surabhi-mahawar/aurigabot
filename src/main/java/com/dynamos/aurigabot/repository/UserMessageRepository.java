package com.dynamos.aurigabot.repository;

import com.dynamos.aurigabot.entity.UserMessage;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface UserMessageRepository extends R2dbcRepository<UserMessage, UUID> {
    Mono<UserMessage> findById(UUID id);

    Mono<UserMessage> save(UserMessage userMessage);

    Flux<UserMessage> findAllByToSourceAndStatusOrderBySentAt(String source, String status);
}
