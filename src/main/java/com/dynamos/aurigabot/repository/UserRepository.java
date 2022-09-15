package com.dynamos.aurigabot.repository;

import com.dynamos.aurigabot.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findFirstByMobile(String mobile);

    Mono<User> findFirstByEmail(String email);
}
