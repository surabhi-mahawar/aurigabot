package com.dynamos.aurigabot.dao;

import com.dynamos.aurigabot.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends CrudRepository<User,Long> {
}
