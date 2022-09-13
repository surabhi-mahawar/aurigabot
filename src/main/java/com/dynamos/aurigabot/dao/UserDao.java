package com.dynamos.aurigabot.dao;

import com.dynamos.aurigabot.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao extends CrudRepository<User,Long> {
    @Query("from User u where day(u.dob) = day(CURRENT_DATE) and month(u.dob) = month(CURRENT_DATE)")
    List<User> getUserByDate();

    
    User findByInterfaceUserId(String key);
}
