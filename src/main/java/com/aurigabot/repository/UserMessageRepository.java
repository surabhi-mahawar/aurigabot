package com.aurigabot.repository;

import com.aurigabot.entity.UserMessage;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserMessageRepository extends R2dbcRepository<UserMessage, UUID> {
    Mono<UserMessage> findById(UUID id);

    Mono<UserMessage> save(UserMessage userMessage);

    @Query("select user_message.*, " +
            "flow.id as fl_id, flow.command_type as fl_command_type, flow.question as fl_question, flow.index as fl_index, flow.payload as fl_payload " +
            "from user_message left join flow on flow.id=user_message.flow " +
            "where to_source = :source and status = :status " +
            "order by sent_at DESC")
    Flux<UserMessage> findAllByToSourceAndStatusOrderBySentAt(@Param("source") String source, @Param("status") String status);

    @Query("select user_message.*, " +
            "flow.id as fl_id, flow.command_type as fl_command_type, flow.question as fl_question, flow.index as fl_index, flow.payload as fl_payload" +
            "from user_message left join flow on flow.id=user_message.flow")
    Flux<UserMessage> findAll();

}
