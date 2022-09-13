package com.dynamos.aurigabot.engine;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dynamos.aurigabot.dao.UserDao;
import com.dynamos.aurigabot.entity.User;

import reactor.core.publisher.Flux;

@Service
public class TelegramService {
	
	@Autowired
	private UserDao userDao;
	
	public List<User> loadAllUserByDOB(){
		return userDao.getUserByDate();
		
	}

}
