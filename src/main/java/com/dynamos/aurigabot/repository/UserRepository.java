package com.dynamos.aurigabot.repository;

import com.dynamos.aurigabot.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Date;
import java.util.UUID;

public interface UserRepository extends R2dbcRepository<User, UUID> {
    Mono<User> findById(String id);

    Mono<User> findFirstByMobile(String mobile);

    Mono<User> findFirstByEmail(String email);

    @Query("select * from users where dob = $1")
    Flux<User> findAllByDob(Date date);

}
