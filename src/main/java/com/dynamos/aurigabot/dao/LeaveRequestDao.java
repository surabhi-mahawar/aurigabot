package com.dynamos.aurigabot.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.dynamos.aurigabot.entity.LeaveRequest;

@Repository
public interface LeaveRequestDao extends CrudRepository<LeaveRequest, Long>{

}
