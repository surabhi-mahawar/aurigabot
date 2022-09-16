package com.dynamos.aurigabot.repository;

import com.dynamos.aurigabot.entity.Flow;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowRepository extends R2dbcRepository<Flow,Long> {
}
