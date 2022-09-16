package com.dynamos.aurigabot.repository;

import com.dynamos.aurigabot.entity.LeaveBalance;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveBalanceRepository extends R2dbcRepository<LeaveBalance,Long> {
}
