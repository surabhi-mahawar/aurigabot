package com.dynamos.aurigabot.service;

import com.dynamos.aurigabot.adapters.AbstractAdapter;
import com.dynamos.aurigabot.entity.UserMessage;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import com.dynamos.aurigabot.repository.UserMessageRepository;
import com.dynamos.aurigabot.repository.UserRepository;
import com.dynamos.aurigabot.response.HttpApiResponse;
import com.dynamos.aurigabot.utils.BotUtil;
import lombok.Builder;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;

@Builder
public class InboundMessageService {

	/* Object of adapter interface to be used for any given adapter */
	private AbstractAdapter adapter;
	private UserRepository userRepository;
	private UserMessageRepository userMessageRepository;

	/**
	 * Process inbound messages
	 * @param message
	 */
	public Mono<HttpApiResponse> processInboundMessage(HttpApiResponse response, Object message) {
		return adapter.convertInboundMsgToMessageFormat(message).map(new Function<UserMessage, Mono<Mono<Mono<HttpApiResponse>>>>() {
			@Override
			public Mono<Mono<Mono<HttpApiResponse>>> apply(UserMessage userMessage) {
				return findUser(userMessage).map(new Function<User, Mono<Mono<HttpApiResponse>>>() {
					@Override
					public Mono<Mono<HttpApiResponse>> apply(User user) {
						userMessage.setFromUserId(user.getId());
						userMessage.setToSource(BotUtil.USER_ADMIN);
						userMessage.setToUserId(UUID.fromString("23738217-fad0-4fcc-8a5a-9fb54df55201"));

						return userMessageRepository.save(userMessage).map(new Function<UserMessage, Mono<HttpApiResponse>>() {
							@Override
							public Mono<HttpApiResponse> apply(UserMessage userMessage) {
								UserMessage outUserMessage = UserMessage.builder()
										.fromUserId(userMessage.getToUserId())
										.toUserId(userMessage.getFromUserId())
										.fromSource(userMessage.getToSource())
										.toSource(userMessage.getFromSource())
										.channel(userMessage.getChannel())
										.provider(userMessage.getProvider())
										.index(0)
										.status(UserMessageStatus.PENDING)
										.message("Hi user, welcome to auriga bot.")
										.build();
								return adapter.sendOutboundMessage(outUserMessage).map(new Function<UserMessage, HttpApiResponse>() {
									@Override
									public HttpApiResponse apply(UserMessage outUserMessage) {
										response.setMessage("Replied sent to user.");
										return response;
									}
								});
							}
						});
					}
				});
			}
		}).flatMap(new Function<Mono<Mono<Mono<HttpApiResponse>>>, Mono<? extends HttpApiResponse>>() {
			@Override
			public Mono<? extends HttpApiResponse> apply(Mono<Mono<Mono<HttpApiResponse>>> m1) {
				return m1.flatMap(new Function<Mono<Mono<HttpApiResponse>>, Mono<? extends HttpApiResponse>>() {
					@Override
					public Mono<? extends HttpApiResponse> apply(Mono<Mono<HttpApiResponse>> m2) {
						return m2.flatMap(new Function<Mono<HttpApiResponse>, Mono<? extends HttpApiResponse>>() {
							@Override
							public Mono<? extends HttpApiResponse> apply(Mono<HttpApiResponse> m3) {
								return m3;
							}
						});
					}
				});
			}
		});
	}

	/**
	 * Find user by from field of user message object
	 * @param msg
	 * @return
	 */
	private Mono<User> findUser(UserMessage msg) {
		return userRepository.findFirstByMobile("7597185708");
//		Long mobile;
//		try{
//			mobile = Long.parseLong(msg.getFromSource());
//		} catch(Exception ex) {
//			mobile = Long.valueOf("0");
//		}
//		if(mobile > 0) {
//			return userRepository.findFirstByMobile(msg.getFromSource());
//		} else {
//			return userRepository.findFirstByEmail(msg.getFromSource());
//		}
	}



}
