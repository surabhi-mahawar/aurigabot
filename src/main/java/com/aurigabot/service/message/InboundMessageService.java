package com.aurigabot.service.message;

import com.aurigabot.adapters.AbstractAdapter;
import com.aurigabot.dto.UserMessageDto;
import com.aurigabot.entity.UserMessage;
import com.aurigabot.entity.User;
import com.aurigabot.enums.ChannelProvider;
import com.aurigabot.repository.FlowRepository;
import com.aurigabot.repository.LeaveRequestRepository;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.repository.UserRepository;
import com.aurigabot.response.HttpApiResponse;
import com.aurigabot.utils.BotUtil;
import com.aurigabot.utils.UserMessageUtil;
import lombok.Builder;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Builder
public class InboundMessageService {

	/* Object of adapter interface to be used for any given adapter */
	private AbstractAdapter adapter;
	private UserRepository userRepository;
	private UserMessageRepository userMessageRepository;
	private FlowRepository flowRepository;
	private LeaveRequestRepository leaveRequestRepository;

	/**
	 * Process inbound message & send reply to user
	 * @param message
	 */
	public Mono<HttpApiResponse> processInboundMessage(HttpApiResponse response, Object message) {
		return adapter.convertInboundMsgToMessageFormat(message).map(new Function<UserMessageDto, Mono<Mono<Mono<Mono<HttpApiResponse>>>>>() {
			@Override
			public Mono<Mono<Mono<Mono<HttpApiResponse>>>> apply(UserMessageDto userMessageDto) {
					return findUser(userMessageDto).map(new Function<List<User>, Mono<Mono<Mono<HttpApiResponse>>>>() {
						@Override
						public Mono<Mono<Mono<HttpApiResponse>>> apply(List<User> users) {
							if(users.size() > 0 && users.get(0) != null) {
								userMessageDto.setFromUserId(users.get(0).getId());
							}
							userMessageDto.setToSource(BotUtil.USER_ADMIN);
							userMessageDto.setToUserId(BotUtil.USER_ADMIN_ID);
	//						System.out.println("This is id: "+userMessageDto.getFlow().getId());

							UserMessage userMessageDao = UserMessageUtil.convertDtotoDao(userMessageDto);
							userMessageDao.setCreatedAt(LocalDateTime.now());
							return userMessageRepository.save(userMessageDao).map(new Function<UserMessage, Mono<Mono<HttpApiResponse>>>() {
								@Override
								public Mono<Mono<HttpApiResponse>> apply(UserMessage userMessage) {

									MessageService messageService = MessageService.builder().userMessageRepository(userMessageRepository).userRepository(userRepository).flowRepository(flowRepository).leaveRequestRepository(leaveRequestRepository).build();
									try {
										User user=null;
										if(users.size() > 0 && users.get(0) != null) {
											user = users.get(0);
										}
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
									} catch (ParseException e) {
										throw new RuntimeException(e);
									}
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
	private Mono<List<User>> findUser(UserMessageDto msg) {
		if (msg.getChannel().name() == ChannelProvider.TELEGRAM.name()) {
			return userRepository.findByTelegramChatId(msg.getFromSource()).collectList();
		} else{
			Long mobile;
			try{
				mobile = Long.parseLong(msg.getFromSource());
			} catch(Exception ex) {
				mobile = Long.valueOf("0");
			}
			if(mobile > 0) {
				return userRepository.findFirstByMobile(msg.getFromSource()).collectList();
			} else {
				return userRepository.findFirstByEmail(msg.getFromSource()).collectList();
			}
		}
	}
}
