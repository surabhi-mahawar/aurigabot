package com.dynamos.aurigabot.repository;

import com.dynamos.aurigabot.entity.Flow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface FlowRepository extends R2dbcRepository<Flow, UUID> {

    @Query("select * from flow where index = $1 and command_type = $2")
    Mono<Flow> findByIndexAndCommandType(Integer index, String commandType);

    Mono<Flow> findByCommmandType(String command);
}
