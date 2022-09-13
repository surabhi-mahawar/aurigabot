package com.dynamos.aurigabot.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.dynamos.aurigabot.entity.Flow;

@Repository
public interface FlowDao extends CrudRepository<Flow, Long>{

}
