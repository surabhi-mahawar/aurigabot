package com.aurigabot.repository;

import com.aurigabot.entity.GoogleTokens;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CalendarUserRepository extends R2dbcRepository<GoogleTokens, Long> {
    Mono<GoogleTokens> findTopByOrderByIdDesc();
    Mono<GoogleTokens> findByEmail(String email);
}
