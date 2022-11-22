package com.aurigabot.repository;

import com.aurigabot.entity.EmployeeManager;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeManagerRepository extends R2dbcRepository<EmployeeManager,Long> {
}
