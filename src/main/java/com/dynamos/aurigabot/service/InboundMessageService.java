package com.dynamos.aurigabot.service;

import com.dynamos.aurigabot.adapters.AbstractAdapter;
import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.entity.UserMessage;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.enums.MessagePayloadType;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import com.dynamos.aurigabot.dto.MessagePayloadChoiceDto;
import com.dynamos.aurigabot.dto.MessagePayloadDto;
import com.dynamos.aurigabot.repository.UserMessageRepository;
import com.dynamos.aurigabot.repository.UserRepository;
import com.dynamos.aurigabot.response.HttpApiResponse;
import com.dynamos.aurigabot.utils.BotUtil;
import com.dynamos.aurigabot.utils.UserMessageUtil;
import lombok.Builder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Builder
public class InboundMessageService {

	/* Object of adapter interface to be used for any given adapter */
	private AbstractAdapter adapter;
	private UserRepository userRepository;
	private UserMessageRepository userMessageRepository;

	/**
	 * Process inbound message & send reply to user
	 * @param message
	 */
	public Mono<HttpApiResponse> processInboundMessage(HttpApiResponse response, Object message) {
		return adapter.convertInboundMsgToMessageFormat(message).map(new Function<UserMessageDto, Mono<Mono<Mono<Mono<HttpApiResponse>>>>>() {
			@Override
			public Mono<Mono<Mono<Mono<HttpApiResponse>>>> apply(UserMessageDto userMessageDto) {
				return findUser(userMessageDto).map(new Function<User, Mono<Mono<Mono<HttpApiResponse>>>>() {
					@Override
					public Mono<Mono<Mono<HttpApiResponse>>> apply(User user) {
						userMessageDto.setFromUserId(user.getId());
						userMessageDto.setToSource(BotUtil.USER_ADMIN);
						userMessageDto.setToUserId(BotUtil.USER_ADMIN_ID);

						UserMessage userMessageDao = UserMessageUtil.convertDtotoDao(userMessageDto);

						return userMessageRepository.save(userMessageDao).map(new Function<UserMessage, Mono<Mono<HttpApiResponse>>>() {
							@Override
							public Mono<Mono<HttpApiResponse>> apply(UserMessage userMessage) {

								MessageService messageService = MessageService.builder().build();
								return messageService.processMessage(user, userMessage).map(new Function<UserMessageDto, Mono<HttpApiResponse>>() {
									@Override
									public Mono<HttpApiResponse> apply(UserMessageDto outUserMessageDto) {
										OutboundMessageService outboundMessageService = OutboundMessageService.builder()
												.adapter(adapter)
												.userMessageRepository(userMessageRepository)
												.build();

										return outboundMessageService.processOutboundMessage(response, outUserMessageDto);
									}
								});
							}
						});
					}
				});
			}
		}).flatMap(new Function<Mono<Mono<Mono<Mono<HttpApiResponse>>>>, Mono<? extends HttpApiResponse>>() {
			@Override
			public Mono<? extends HttpApiResponse> apply(Mono<Mono<Mono<Mono<HttpApiResponse>>>> m1) {
				return m1.flatMap(new Function<Mono<Mono<Mono<HttpApiResponse>>>, Mono<? extends HttpApiResponse>>() {
					@Override
					public Mono<? extends HttpApiResponse> apply(Mono<Mono<Mono<HttpApiResponse>>> m2) {
						return m2.flatMap(new Function<Mono<Mono<HttpApiResponse>>, Mono<? extends HttpApiResponse>>() {
							@Override
							public Mono<? extends HttpApiResponse> apply(Mono<Mono<HttpApiResponse>> m3) {
								return m3.flatMap(new Function<Mono<HttpApiResponse>, Mono<? extends HttpApiResponse>>() {
									@Override
									public Mono<? extends HttpApiResponse> apply(Mono<HttpApiResponse> m4) {
										return m4;
									}
								});
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
	private Mono<User> findUser(UserMessageDto msg) {
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
