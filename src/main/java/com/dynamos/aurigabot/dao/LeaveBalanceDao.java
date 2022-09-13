package com.dynamos.aurigabot.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.dynamos.aurigabot.entity.LeaveBalance;

@Repository
public interface LeaveBalanceDao extends CrudRepository<LeaveBalance, Long>{

}
