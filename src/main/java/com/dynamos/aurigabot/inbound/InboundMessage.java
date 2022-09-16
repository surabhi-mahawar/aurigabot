package com.dynamos.aurigabot.inbound;

import com.dynamos.aurigabot.adapters.AbstractAdapter;
import com.dynamos.aurigabot.entity.UserMessage;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.repository.UserRepository;
import com.dynamos.aurigabot.utils.BotUtil;
import lombok.Builder;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Builder
public class InboundMessage{

	/* Object of adapter interface to be used for any given adapter */
	private AbstractAdapter adapter;
	private UserRepository userRepository;

	/**
	 * Process inbound messages
	 * @param message
	 */
	public void process(Object message) {
		adapter.convertInboundMsgToMessageFormat(message).subscribe(userMessage -> {
//			findUser(userMessage).subscribe(user -> {
//				userMessage.setFromUserId(user.getId());
//			});
//			System.out.println("User not found");
			userMessage.setToSource(userMessage.getFromSource());
			userMessage.setFromSource(BotUtil.USER_ADMIN);
			adapter.convertOutboundMsgFromMessageFormat(userMessage).subscribe();
		});
	}

	/**
	 * Find user by from field of user message object
	 * @param msg
	 * @return
	 */
	private Mono<User> findUser(UserMessage msg) {
		Long mobile;
		try{
			mobile = Long.parseLong(msg.getFromSource());
		} catch(Exception ex) {
			mobile = Long.valueOf("0");
		}
		if(mobile > 0) {
			return userRepository.findFirstByMobile(msg.getFromSource());
		} else {
			return userRepository.findFirstByEmail(msg.getFromSource());
		}
	}



}
