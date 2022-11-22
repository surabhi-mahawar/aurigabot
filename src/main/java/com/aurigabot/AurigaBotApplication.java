package com.aurigabot;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories
public class AurigaBotApplication implements ApplicationRunner {


//	@Autowired
//	private CustomUserService customUserService;
	public static void main(String[] args) {

		SpringApplication.run(AurigaBotApplication.class, args);

	}



	@Override
	public void run(ApplicationArguments args) throws Exception {
//		customUserService.addSuperAdmin();
	}
}


