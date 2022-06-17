package com.dynamos.aurigabot;

import com.dynamos.aurigabot.entity.AMessage;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.enums.State;
import com.dynamos.aurigabot.enums.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.List;

@SpringBootApplication
public class DynamosApplication {

	public static void main(String[] args) throws JsonProcessingException {
		SpringApplication.run(DynamosApplication.class, args);

		AMessage message = new AMessage();
		message.setId(1l);
		message.setCommand("/cmd");
		message.setOptions("na");
		message.setReceivedAt(new Date());
		message.setBotName("aurigaBot");
		message.setState(State.RECEIVED);
		message.setText("this is text");
		message.setUser(null);

		ObjectMapper objectMapper = new ObjectMapper();
//		System.out.println(objectMapper.writeValueAsString(message));

		String str1 = objectMapper.writeValueAsString(message);
		JsonNode jsonNode1 = objectMapper.readTree(str1);
		AMessage message1 = objectMapper.readValue(jsonNode1.toString(), AMessage.class);
		System.out.println(message1);

	}

}
