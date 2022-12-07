package com.aurigabot.service.message;

import com.aurigabot.adapters.AbstractAdapter;
import com.aurigabot.adapters.TelegramAdapter;
import com.aurigabot.dto.UserMessageDto;
import com.aurigabot.entity.UserMessage;
import com.aurigabot.entity.User;
import com.aurigabot.enums.ChannelProvider;
import com.aurigabot.enums.MessageChannel;
import com.aurigabot.providers.AdapterFactoryProvider;
import com.aurigabot.repository.FlowRepository;
import com.aurigabot.repository.LeaveRequestRepository;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.repository.UserRepository;
import com.aurigabot.response.HttpApiResponse;
import com.aurigabot.service.KafkaProducerService;
import com.aurigabot.utils.BotUtil;
import com.aurigabot.utils.UserMessageUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.tuple.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.web.JsonProjectingMethodInterceptorFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class InboundMessageService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserMessageRepository userMessageRepository;

	@Autowired
	private AdapterFactoryProvider adapterFactoryProvider;

	@Autowired
	private KafkaProducerService kafkaProducerService;

	@Value(value = "${kafka.topic.user.message}")
	private String messageTopic;

	@KafkaListener(topics = "${kafka.topic.telegram.inbound.message}", groupId = "${kafka.inbound.consumer.group.id}")
	public void listenTelegramInboundTopic(String message) {
		log.info("Received Inbound Message: " + message);
		AbstractAdapter adapter = adapterFactoryProvider.getAdapter(MessageChannel.TELEGRAM);
		ObjectMapper mapper = new ObjectMapper();
		try {
			if(adapter != null) {
				Object obj = mapper.readValue(message, Message.class);
				processInboundMessage(adapter, obj).subscribe(booleanObjectPair -> {
					try {
						if(booleanObjectPair.getLeft() == true) {
							UserMessage msg = (UserMessage) booleanObjectPair.getRight();
							String jsonStr = mapper.writeValueAsString(msg);
							kafkaProducerService.sendMessage(jsonStr, messageTopic);
						} else {
							log.error(booleanObjectPair.getRight().toString());
						}

					} catch (JsonProcessingException e) {
						log.error("JsonProcessingException in listenTelegramInboundTopic outbound: "+e.getMessage());
					}
				});
			} else {
				log.error("Adapter is null for telegram channel.");
			}
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in listenTelegramInboundTopic: "+e.getMessage());
		} catch (HttpServerErrorException.InternalServerError e) {
			log.error("InternalServerError in listenTelegramInboundTopic: "+e.getMessage());
		}
	}

	/**
	 * Process inbound message & send reply to user
	 * @param message
	 */
	public Mono<Pair<Boolean, Object>> processInboundMessage(AbstractAdapter adapter, Object message) {
		return adapter.convertInboundMsgToMessageFormat(message).map(new Function<UserMessageDto, Mono<Mono<Pair<Boolean, Object>>>>() {
			@Override
			public Mono<Mono<Pair<Boolean, Object>>> apply(UserMessageDto userMessageDto) {
					return findUser(userMessageDto).map(new Function<List<User>, Mono<Pair<Boolean, Object>>>() {
						@Override
						public Mono<Pair<Boolean, Object>> apply(List<User> users) {
							if(users.size() > 0 && users.get(0) != null) {
								userMessageDto.setFromUserId(users.get(0).getId());
							}
							userMessageDto.setToSource(BotUtil.USER_ADMIN);
							userMessageDto.setToUserId(BotUtil.USER_ADMIN_ID);
							UserMessage userMessageDao = UserMessageUtil.convertDtotoDao(userMessageDto);
							userMessageDao.setCreatedAt(LocalDateTime.now());
							return userMessageRepository.save(userMessageDao).map(new Function<UserMessage, Pair<Boolean, Object>>() {
								@Override
								public Pair<Boolean, Object> apply(UserMessage userMessage) {
									return Pair.of(true, userMessage);
//									MessageService messageService = MessageService.builder().userMessageRepository(userMessageRepository).userRepository(userRepository).flowRepository(flowRepository).leaveRequestRepository(leaveRequestRepository).build();
//									try {
//										User user=null;
//										if(users.size() > 0 && users.get(0) != null) {
//											user = users.get(0);
//										}
//										return messageService.processMessage(user, userMessage).map(new Function<UserMessageDto, Mono<HttpApiResponse>>() {
//											@Override
//											public Mono<HttpApiResponse> apply(UserMessageDto outUserMessageDto) {
//												OutboundMessageService outboundMessageService = OutboundMessageService.builder()
//														.adapter(adapter)
//														.userMessageRepository(userMessageRepository)
//														.build();
//
//												return outboundMessageService.processOutboundMessage(response, outUserMessageDto);
//											}
//										});
//									} catch (ParseException e) {
//										throw new RuntimeException(e);
//									}
								}
							});
						}
				});
			}
		}).flatMap(new Function<Mono<Mono<Pair<Boolean, Object>>>, Mono<? extends Pair<Boolean, Object>>>() {
			@Override
			public Mono<? extends Pair<Boolean, Object>> apply(Mono<Mono<Pair<Boolean, Object>>> m1) {
				return m1.flatMap(new Function<Mono<Pair<Boolean, Object>>, Mono<? extends Pair<Boolean, Object>>>() {
					@Override
					public Mono<? extends Pair<Boolean, Object>> apply(Mono<Pair<Boolean, Object>> m2) {
						return m2;
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
