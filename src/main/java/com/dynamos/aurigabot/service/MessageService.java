package com.dynamos.aurigabot.service;

import com.dynamos.aurigabot.dto.MessagePayloadDto;
import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.entity.UserMessage;
import com.dynamos.aurigabot.enums.*;
import com.dynamos.aurigabot.repository.FlowRepository;
import com.dynamos.aurigabot.repository.LeaveRequestRepository;
import com.dynamos.aurigabot.repository.UserMessageRepository;
import com.dynamos.aurigabot.repository.UserRepository;
import com.dynamos.aurigabot.service.command.BirthdayService;
import com.dynamos.aurigabot.service.command.DashboardService;
import com.dynamos.aurigabot.service.command.LeaveRequestService;
import com.dynamos.aurigabot.service.command.TelegramService;
import com.dynamos.aurigabot.utils.BotUtil;
import com.dynamos.aurigabot.utils.UserMessageUtil;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;

@Builder
public class MessageService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private UserMessageRepository userMessageRepository;

    @Autowired
    private UserMessageUtil userMessageUtil;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;


    /**
     * Process incoming message, return outgoing message
     * @param user
     * @param incomingUserMessage
     * @return
     */
    public Mono<UserMessageDto> processMessage(User user, UserMessage incomingUserMessage) throws ParseException {
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

        if(user == null && incomingUserMessage.getChannel()== MessageChannel.TELEGRAM) {
            TelegramService telegramService = TelegramService.builder()
                    .userMessageRepository(userMessageRepository)
                    .flowRepository(flowRepository)
                    .userRepository(userRepository)
                    .build();
            return telegramService.processUnregisteredTelegramUser(incomingUserMessage, outUserMessageDto);
        } else if (BotUtil.isBotStartingMessage(incomingUserMessage.getMessage())) {
            return processBotStartingMessage(user, outUserMessageDto);
        } else if(commandType != null) {
            return processCommand(user, outUserMessageDto, commandType, incomingUserMessage);
        } else {
            return getLastSentMessage(incomingUserMessage.getFromSource()).map(new Function<UserMessage, Mono<UserMessageDto>>() {
                @Override
                public Mono<UserMessageDto> apply(UserMessage lastMessage) {
                    if(lastMessage.getFlow().getCommandType().equals(CommandType.LEAVE)) {
                        LeaveRequestService leaveRequestService = LeaveRequestService.builder()
                                .userMessageRepository(userMessageRepository)
                                .flowRepository(flowRepository)
                                .leaveRequestRepository(leaveRequestRepository)
                                .build();
                        return leaveRequestService.processLeaveRequest(user, incomingUserMessage, outUserMessageDto, lastMessage);
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
        String msgText = "Hello "+user.getName()+", \n Please select a option from the list to proceed further.";
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
            BirthdayService birthdayService = BirthdayService.builder()
                    .userRepository(userRepository)
                    .build();
            return birthdayService.processBirthdayRequest( userMessageDto,"/birthday", 0);
        } else if (commandType.equals(CommandType.LEAVE)) {
            LeaveRequestService leaveRequestService = LeaveRequestService.builder()
                    .userMessageRepository(userMessageRepository)
                    .flowRepository(flowRepository)
                    .leaveRequestRepository(leaveRequestRepository)
                    .build();
            return leaveRequestService.processLeaveRequest(user, incomingMessageDto, userMessageDto, null);
        } else if (commandType.equals(CommandType.DASHBOARD)) {
            DashboardService dashboardService = DashboardService.builder()
                    .userRepository(userRepository)
                    .leaveRequestRepository(leaveRequestRepository)
                    .build();
            return dashboardService.processDashboardRequest(userMessageDto,"/dashboard",0,user);
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
}
