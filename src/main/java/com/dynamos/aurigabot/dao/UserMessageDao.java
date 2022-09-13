package com.dynamos.aurigabot.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.dynamos.aurigabot.entity.UserMessage;

@Repository
public interface UserMessageDao extends CrudRepository<UserMessage, Long>{

}
