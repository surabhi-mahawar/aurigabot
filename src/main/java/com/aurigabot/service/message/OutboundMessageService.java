package com.aurigabot.service.message;

import com.aurigabot.adapters.AbstractAdapter;
import com.aurigabot.dto.UserMessageDto;
import com.aurigabot.entity.UserMessage;
import com.aurigabot.enums.UserMessageStatus;
import com.aurigabot.providers.AdapterFactoryProvider;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.response.HttpApiResponse;
import com.aurigabot.utils.UserMessageUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.function.Function;

@Slf4j
@Service
public class OutboundMessageService {
    @Autowired
    private UserMessageRepository userMessageRepository;

    @Autowired
    private AdapterFactoryProvider adapterFactoryProvider;

    @KafkaListener(topics = "${kafka.topic.outbound.message}", groupId = "${kafka.outbound.consumer.group.id}")
    public void listenOutboundMessageTopic(String message) {
        log.info("Received Outbound Message: " + message);
        ObjectMapper mapper = new ObjectMapper();
        try {
            UserMessageDto userMessageDto = mapper.readValue(message, UserMessageDto.class);
            AbstractAdapter adapter = adapterFactoryProvider.getAdapter(userMessageDto.getChannel());
            processOutboundMessage(adapter, userMessageDto).subscribe(booleanObjectPair -> {
                if(booleanObjectPair.getLeft() == true) {
                    log.info(booleanObjectPair.getRight().toString());
                } else {
                    log.error(booleanObjectPair.getRight().toString());
                }
            });
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException in listenOutboundMessageTopic: "+e.getMessage());
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("InternalServerError in listenOutboundMessageTopic: "+e.getMessage());
        }
    }

    /**
     * Process outbound message - convert to channel message format and send message to user
     * @param userMessageDto
     * @return
     */
    public Mono<Pair<Boolean, Object>> processOutboundMessage(AbstractAdapter adapter, UserMessageDto userMessageDto) {
        return adapter.sendOutboundMessage(userMessageDto).map(new Function<UserMessageDto, Mono<Pair<Boolean, Object>>>() {
            @Override
            public Mono<Pair<Boolean, Object>> apply(UserMessageDto userMessageDto) {
                UserMessage userMessageDao = UserMessageUtil.convertDtotoDao(userMessageDto);
                userMessageDao.setSentAt(LocalDateTime.now());
                userMessageDao.setCreatedAt(LocalDateTime.now());
                return userMessageRepository.save(userMessageDao).map(new Function<UserMessage, Pair<Boolean, Object>>() {
                    @Override
                    public Pair<Boolean, Object> apply(UserMessage userMessage) {
                        if(userMessage.getStatus().equals(UserMessageStatus.SENT)) {
                            return Pair.of(true, "Reply message sent to user.");
                        } else {
                            return Pair.of(false, "Reply message not sent to user.");
                        }
                    }
                });
            }
        }).flatMap(new Function<Mono<Pair<Boolean, Object>>, Mono<? extends Pair<Boolean, Object>>>() {
            @Override
            public Mono<? extends Pair<Boolean, Object>> apply(Mono<Pair<Boolean, Object>> m1) {
                return m1;
            }
        });
    }
}
