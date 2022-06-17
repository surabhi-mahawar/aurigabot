package com.dynamos.aurigabot.dao;

import com.dynamos.aurigabot.entity.AMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AMessageDao extends CrudRepository<AMessage,Long> {
}
