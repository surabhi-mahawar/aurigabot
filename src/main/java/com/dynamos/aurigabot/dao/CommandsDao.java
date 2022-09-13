package com.dynamos.aurigabot.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.dynamos.aurigabot.entity.Commands;

@Repository
public interface CommandsDao extends CrudRepository<Commands, Long>{
	

}
