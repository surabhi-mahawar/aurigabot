package com.dynamos.aurigabot.repository;

import com.dynamos.aurigabot.entity.LeaveRequest;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends R2dbcRepository<LeaveRequest,Long> {
}
