package com.aurigabot.service.message;

import com.aurigabot.adapters.AbstractAdapter;
import com.aurigabot.dto.UserMessageDto;
import com.aurigabot.entity.UserMessage;
import com.aurigabot.entity.User;
import com.aurigabot.enums.ChannelProvider;
import com.aurigabot.enums.MessageChannel;
import com.aurigabot.providers.AdapterFactoryProvider;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.repository.UserRepository;
import com.aurigabot.service.KafkaProducerService;
import com.aurigabot.utils.BotUtil;
import com.aurigabot.utils.UserMessageUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;

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

	/**
	 * Process inbound messages - for telegram inbound message kafka topic
	 * @param message
	 */
	@KafkaListener(topics = "${kafka.topic.telegram.inbound.message}", groupId = "${kafka.inbound.consumer.group.id}")
	public void listenTelegramInboundTopic(String message) {
		log.info("Received Telegram Inbound Message: " + message);
		ObjectMapper mapper = new ObjectMapper();
		try {
			Object obj = mapper.readValue(message, Message.class);
			processInboundMessageTopic(MessageChannel.TELEGRAM, obj);
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in listenTelegramInboundTopic: "+e.getMessage());
		} catch (HttpServerErrorException.InternalServerError e) {
			log.error("InternalServerError in listenTelegramInboundTopic: "+e.getMessage());
		}
	}

	/**
	 * Process inbound messages - for netcore whatsapp inbound message kafka topic
	 * @param message
	 */
	@KafkaListener(topics = "${kafka.topic.netcore.whatsapp.inbound.message}", groupId = "${kafka.inbound.consumer.group.id}")
	public void listenNetcoreWhatsappInboundTopic(String message) {
		log.info("Received Netcore Whatsapp Inbound Message: " + message);
		ObjectMapper mapper = new ObjectMapper();
		try {
			Object obj = mapper.readValue(message, com.aurigabot.dto.netcore.whatsapp.inbound.Message.class);
			processInboundMessageTopic(MessageChannel.WHATSAPP, obj);
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in listenNetcoreWhatsappInboundTopic: "+e.getMessage());
		} catch (HttpServerErrorException.InternalServerError e) {
			log.error("InternalServerError in listenNetcoreWhatsappInboundTopic: "+e.getMessage());
		}
	}

	/**
	 * Process inbound message, send next request to kafka topic
	 * @param channel
	 * @param message
	 */
	public void processInboundMessageTopic(MessageChannel channel, Object message) {
		AbstractAdapter adapter = adapterFactoryProvider.getAdapter(channel);
		ObjectMapper mapper = new ObjectMapper();
		try {
			if(adapter != null) {
				processInboundMessage(adapter, message).subscribe(booleanObjectPair -> {
					try {
						if(booleanObjectPair.getLeft() == true) {
							UserMessage msg = (UserMessage) booleanObjectPair.getRight();
							String jsonStr = mapper.writeValueAsString(msg);
							kafkaProducerService.sendMessage(jsonStr, messageTopic);
						} else {
							log.error(booleanObjectPair.getRight().toString());
						}
					} catch (JsonProcessingException e) {
						log.error("JsonProcessingException in processInboundMessageTopic outbound: "+e.getMessage());
					}
				});
			} else {
				log.error("Adapter is null for channel: "+channel.toString());
			}
		} catch (HttpServerErrorException.InternalServerError e) {
			log.error("InternalServerError in processInboundMessageTopic: "+e.getMessage());
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
