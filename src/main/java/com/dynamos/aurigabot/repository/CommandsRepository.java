package com.dynamos.aurigabot.repository;

import com.dynamos.aurigabot.entity.Commands;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandsRepository extends R2dbcRepository<Commands,Long> {

}