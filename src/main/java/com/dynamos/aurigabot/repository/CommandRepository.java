package com.dynamos.aurigabot.repository;

import com.dynamos.aurigabot.entity.Command;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandRepository extends R2dbcRepository<Command,Long> {

}