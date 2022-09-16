package com.dynamos.aurigabot.repository;

import com.dynamos.aurigabot.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends R2dbcRepository<User, UUID> {
    Mono<User> findFirstByMobile(String mobile);

    Mono<User> findFirstByEmail(String email);
}
