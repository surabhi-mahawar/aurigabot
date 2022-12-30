package com.aurigabot.service.message;

import com.aurigabot.dto.MessagePayloadDto;
import com.aurigabot.dto.UserMessageDto;
import com.aurigabot.entity.User;
import com.aurigabot.entity.UserMessage;
import com.aurigabot.enums.*;
import com.aurigabot.repository.LeaveRequestRepository;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.repository.UserRepository;
import com.aurigabot.service.KafkaProducerService;
import com.aurigabot.service.command.*;
import com.aurigabot.utils.BotUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Mono;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class MessageReplyService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMessageRepository userMessageRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private TelegramService telegramService;

    @Autowired
    private BirthdayService birthdayService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private EventsService eventsService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Value(value = "${kafka.topic.outbound.message}")
    private String outboundTopic;

    @KafkaListener(topics = "${kafka.topic.user.message}", groupId = "${kafka.user.message.consumer.group.id}")
    public void listenUserMessageTopic(String message) {
        log.info("Received User Message: " + message);
        ObjectMapper mapper = new ObjectMapper();
        try {
            UserMessage userMessage = mapper.readValue(message, UserMessage.class);
            processMessage(userMessage).subscribe(booleanObjectPair -> {
                try {
                    if(booleanObjectPair.getLeft() == true) {
                        UserMessageDto dto = (UserMessageDto) booleanObjectPair.getRight();
                        String jsonStr = mapper.writeValueAsString(dto);
                        kafkaProducerService.sendMessage(jsonStr, outboundTopic);
                        if (dto.getMessage().contains("submitted")){
                            leaveRequestService.notifyManager(dto).subscribe();
                        }
                    } else {
                        log.error(booleanObjectPair.getRight().toString());
                    }

                } catch (JsonProcessingException e) {
                    log.error("JsonProcessingException in listenUserMessageTopic: "+e.getMessage());
                }
            });
        } catch (ParseException e) {
            log.error("ParseException in listenUserMessageTopic: "+e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException in listenUserMessageTopic: "+e.getMessage());
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("InternalServerError in listenUserMessageTopic: "+e.getMessage());
        }
    }

    /**
     * Process incoming message, return outgoing message
     * @param incomingUserMessage
     * @return
     */
    public Mono<Pair<Boolean, Object>> processMessage(UserMessage incomingUserMessage) throws ParseException {
        return findUserById(incomingUserMessage.getFromUserId()).map(new Function<List<User>, Mono<Pair<Boolean, Object>>>() {
            @Override
            public Mono<Pair<Boolean, Object>> apply(List<User> users) {
                User user=null;
                if(users.size() > 0 && users.get(0) != null) {
                    user = users.get(0);
                }

                return processMessageInternal(user, incomingUserMessage).map(new Function<UserMessageDto, Pair<Boolean, Object>>() {
                    @Override
                    public Pair<Boolean, Object> apply(UserMessageDto userMessageDto) {
                        return Pair.of(true, userMessageDto);
                    }
                });
            }
        }).flatMap(new Function<Mono<Pair<Boolean, Object>>, Mono<? extends Pair<Boolean, Object>>>() {
            @Override
            public Mono<? extends Pair<Boolean, Object>> apply(Mono<Pair<Boolean, Object>> pairMono) {
                return pairMono;
            }
        });
    }

    private Mono<UserMessageDto> processMessageInternal(User user, UserMessage incomingUserMessage) {
        UserMessageDto outUserMessageDto = UserMessageDto.builder()
                .fromUserId(incomingUserMessage.getToUserId())
                .toUserId(incomingUserMessage.getFromUserId())
                .fromSource(incomingUserMessage.getToSource())
                .toSource(incomingUserMessage.getFromSource())
                .channel(incomingUserMessage.getChannel())
                .provider(incomingUserMessage.getProvider())
                .index(0)
                .status(UserMessageStatus.PENDING)
                .build();
        CommandType commandType = BotUtil.isCommand(incomingUserMessage.getMessage());

        /**
         * If user is not found, proceed with register telegram chat id of user flow
         */

        if(user == null) {
            if(incomingUserMessage.getChannel()== MessageChannel.TELEGRAM) {
                return telegramService.processUnregisteredTelegramUser(incomingUserMessage, outUserMessageDto);
            } else {
                String msgText = "We could not identify you. We cannot proceed with your request.";
                outUserMessageDto.setMessage(msgText);

                MessagePayloadDto payload = MessagePayloadDto.builder()
                        .message(msgText)
                        .msgType(MessagePayloadType.TEXT)
                        .build();
                outUserMessageDto.setPayload(payload);

                return Mono.just(outUserMessageDto);
            }
        } else if (BotUtil.isBotStartingMessage(incomingUserMessage.getMessage())) {
            return processBotStartingMessage(user, outUserMessageDto);
        } else if(commandType != null) {
            return processCommand(user, outUserMessageDto, commandType, incomingUserMessage);
        } else {
            return getLastSentMessage(incomingUserMessage.getFromSource()).map(new Function<UserMessage, Mono<UserMessageDto>>() {
                @Override
                public Mono<UserMessageDto> apply(UserMessage lastMessage) {
                    if(lastMessage.getFlow() != null && lastMessage.getFlow().getCommandType().equals(CommandType.LEAVEREQUEST)) {
                        return leaveRequestService.processApplyLeaveRequest(user, incomingUserMessage, outUserMessageDto, lastMessage);

                    } else if(lastMessage.getFlow() != null && lastMessage.getFlow().getCommandType().equals(CommandType.BIRTHDAY)) {
                        return birthdayService.processNewBirthdayRequest(incomingUserMessage, outUserMessageDto, lastMessage);
                    } else if(lastMessage.getFlow() != null && (lastMessage.getFlow().getCommandType().equals(CommandType.LISTEVENTS) || lastMessage.getFlow().getCommandType().equals(CommandType.CREATEEVENT))) {
                        return eventsService.processEventsRequest(user, incomingUserMessage, outUserMessageDto, null, lastMessage);
                    } else {
                        return processInvalidRequest(outUserMessageDto);
                    }
                }
            }).flatMap(new Function<Mono<UserMessageDto>, Mono<? extends UserMessageDto>>() {
                @Override
                public Mono<? extends UserMessageDto> apply(Mono<UserMessageDto> userMessageDtoMono) {
                    return userMessageDtoMono;
                }
            });
        }
    }

    /**
     * Find user by id
     * @param userId
     * @return
     */
    private Mono<List<User>> findUserById(@Nullable UUID userId) {
        if(userId != null) {
            return userRepository.findById(userId).flux().collectList();
        } else {
            List<User> list = new ArrayList<>();
            return Mono.just(list);
        }
    }

    /**
     * Get last message sent to user
     * @param toSource
     * @return
     */
    public Mono<UserMessage> getLastSentMessage(String toSource) {
        return userMessageRepository.findAllByToSourceAndStatusOrderBySentAt(toSource, UserMessageStatus.SENT.name())
                .collectList()
                .map(new Function<List<UserMessage>, UserMessage>() {
                    @Override
                    public UserMessage apply(List<UserMessage> userMessages) {
                        if(userMessages.size() > 0 && userMessages.get(0) != null) {
                            return userMessages.get(0);
                        }
                        return null;
                    }
                });
    }

    /**
     * Process starting message to return list of commands for bot
     * @param user
     * @param userMessageDto
     * @return
     */
    public Mono<UserMessageDto> processBotStartingMessage(User user, UserMessageDto userMessageDto) {
        String msgText = "Hello "+user.getName()+", \nPlease select a option from the list to proceed further.\n";
        userMessageDto.setMessage(msgText);

        MessagePayloadDto payload = MessagePayloadDto.builder()
                .message(msgText)
                .msgType(MessagePayloadType.TEXT)
                .choices(BotUtil.getCommandChoices())
                .build();
        userMessageDto.setPayload(payload);

        return Mono.just(userMessageDto);
    }

    /**
     * Process if the message is a command, return a reply for command
     * @param user
     * @param userMessageDto
     * @param commandType
     * @return
     */
    public Mono<UserMessageDto> processCommand(User user, UserMessageDto userMessageDto, CommandType commandType,UserMessage incomingMessageDto) {
        if(commandType.equals(CommandType.BIRTHDAY)) {
            return birthdayService.processNewBirthdayRequest( incomingMessageDto, userMessageDto, null);
        } else if (commandType.equals(CommandType.LEAVEREQUEST)) {
            return leaveRequestService.processApplyLeaveRequest(user, incomingMessageDto, userMessageDto, null);
        } else if (commandType.equals(CommandType.DASHBOARD)) {
            return dashboardService.processDashboardRequest(userMessageDto,"/dashboard",0,user);
        } else if (commandType.equals(CommandType.TODO)) {
            return processToDoRequest(userMessageDto);
        } else if(commandType.equals(CommandType.EVENTS) || commandType.equals(CommandType.LISTEVENTS) || commandType.equals(CommandType.CREATEEVENT)){
            return eventsService.processEventsRequest(user, incomingMessageDto, userMessageDto, commandType, null);
        }else if (commandType.equals(CommandType.APPROVE)) {
            return leaveRequestService.handleLeaveRequest(user, userMessageDto, commandType);
        }else if (commandType.equals(CommandType.REJECT)) {
            return leaveRequestService.handleLeaveRequest(user, userMessageDto, commandType);
        } else {
            return processInvalidRequest(userMessageDto);
        }
    }

    /**
     * Process unidentified/invalid request
     * @param userMessageDto
     * @return
     */
    private Mono<UserMessageDto> processInvalidRequest(UserMessageDto userMessageDto) {
        return Mono.just(BotUtil.getInvalidRequestMessageDto(userMessageDto));
    }

    private Mono<UserMessageDto> processToDoRequest(UserMessageDto userMessageDto) {
        String msgText = "This feature is coming soon!";
        userMessageDto.setMessage(msgText);

        MessagePayloadDto payload = MessagePayloadDto.builder()
                .message(msgText)
                .msgType(MessagePayloadType.TEXT)
                .build();
        userMessageDto.setPayload(payload);

        return Mono.just(userMessageDto);
    }
}
