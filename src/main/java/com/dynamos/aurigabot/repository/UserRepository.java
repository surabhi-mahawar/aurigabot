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

    Flux<User> findFirstByMobile(String mobile);

    Flux<User> findByTelegramChatId(String chatId);
    Flux<User> findFirstByEmail(String   email);

    //    @Query("select * from users where EXTRACT(day from dob) = EXTRACT(day from $1) and EXTRACT(month from dob) = EXTRACT(month from $1)")
//    @Query("select * from users where EXTRACT(day from dob) = EXTRACT(day from $1) and EXTRACT(month from dob) = EXTRACT(month from $1)")
    Flux<User> findAllByDob(Date date);

}
