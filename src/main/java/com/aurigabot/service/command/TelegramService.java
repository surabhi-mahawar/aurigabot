package com.aurigabot.service.command;

import com.aurigabot.entity.Flow;
import com.aurigabot.entity.User;
import com.aurigabot.entity.UserMessage;
import com.aurigabot.enums.CommandType;
import com.aurigabot.enums.UserMessageStatus;
import com.aurigabot.repository.FlowRepository;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.repository.UserRepository;
import com.aurigabot.utils.BotUtil;
import com.aurigabot.dto.MessagePayloadDto;
import com.aurigabot.dto.UserMessageDto;
import com.aurigabot.enums.MessagePayloadType;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Service
public class TelegramService {
    @Autowired
    private UserMessageRepository userMessageRepository;

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Process unregistered Telegram User - register user and save his/her telegram chat id for future coversations
     * @param incomingUserMessage
     * @param outUserMessageDto
     * @return
     */
    public Mono<UserMessageDto> processUnregisteredTelegramUser(UserMessage incomingUserMessage, UserMessageDto outUserMessageDto) {
        return userMessageRepository.findAllByToSourceAndStatusOrderBySentAt(incomingUserMessage.getFromSource(), UserMessageStatus.SENT.name()).collectList().map(new Function<List<UserMessage>, Mono<UserMessageDto>>(){
            @Override
            public Mono<UserMessageDto> apply(List<UserMessage> userMessages){
                /**
                 * If last message sent to user is empty, proceed with registration request flow
                 * Else If last message sent to user does not have flow, or its command type is not /regTelegramUser, proceed with registration request flow
                 * Else proceed with telegram chat id registration process for the message received from user
                 */
                if(userMessages.size()==0){
                    return processUserRegistration(outUserMessageDto);
                } else if(userMessages.get(0).getFlow() == null || !userMessages.get(0).getFlow().getCommandType().equals(CommandType.REGTELEGRAMUSER)){
                    return processUserRegistration(outUserMessageDto);
                } else {
                    /**
                     * If last message sent to user have flow and its command type is /regTelegramUser,
                     * check for existing user email by received message from user
                     * Else proceed with invalid request flow
                     */
                    if (userMessages.get(0).getFlow() != null && userMessages.get(0).getFlow().getCommandType().equals(CommandType.REGTELEGRAMUSER)){
                        return userRepository.findFirstByEmail(incomingUserMessage.getMessage()).collectList().map(new Function<List<User>,Mono<UserMessageDto>>() {
                            @Override
                            public Mono<UserMessageDto> apply(List<User> users){
                                if(users.size()==0){
                                    return processUserRegistration(outUserMessageDto);
                                } else{
                                    User user = users.get(0);
                                    user.setTelegramChatId(incomingUserMessage.getFromSource());
                                    return userRepository.save(user).map(new Function<User, UserMessageDto>() {
                                        @Override
                                        public UserMessageDto apply(User user) {
                                            return getRegistrationSuccessfulMessageDto(outUserMessageDto);
                                        }
                                    });
                                }
                            }
                        }).flatMap(new Function<Mono<UserMessageDto>, Mono<? extends UserMessageDto>>() {
                            @Override
                            public Mono<? extends UserMessageDto> apply(Mono<UserMessageDto> userMessageDtoMono) {
                                return userMessageDtoMono;
                            }
                        });
                    } else {
                        return Mono.just(BotUtil.getInvalidRequestMessageDto(outUserMessageDto));
                    }
                }
            }
        }).flatMap(new Function<Mono<UserMessageDto>, Mono<? extends UserMessageDto>>() {
            @Override
            public Mono<? extends UserMessageDto> apply(Mono<UserMessageDto> userMessageDtoMono) {
                return userMessageDtoMono;
            }
        });
    }

    /**
     * Process user registration - Send a question to ask for question for registration process
     * @param userMessageDto
     * @return
     */
    private Mono<UserMessageDto> processUserRegistration(UserMessageDto userMessageDto) {
        return flowRepository.findByIndexAndCommandType(0, CommandType.REGTELEGRAMUSER.getDisplayValue()).collectList().map(new Function<List<Flow>, UserMessageDto>() {
            @Override
            public UserMessageDto apply(List<Flow> flow) {
                if(flow.get(0) != null) {
                    userMessageDto.setMessage(flow.get(0).getQuestion());

                    userMessageDto.setFlow(flow.get(0));
                    MessagePayloadDto payload = MessagePayloadDto.builder()
                            .message(flow.get(0).getQuestion())
                            .msgType(MessagePayloadType.TEXT)
                            .build();

                    userMessageDto.setPayload(payload);
                    return userMessageDto;
                } else {
                    return BotUtil.getInvalidRequestMessageDto(userMessageDto);
                }
            }
        });
    }

    /**
     * Get registration successful message
     * @param userMessageDto
     * @return
     */
    private UserMessageDto getRegistrationSuccessfulMessageDto(UserMessageDto userMessageDto) {
        String msgText = "Your identity is confirmed, Please select one choice from the list below.\n";
        userMessageDto.setMessage(msgText);

        MessagePayloadDto payload = MessagePayloadDto.builder()
                .message(msgText)
                .msgType(MessagePayloadType.TEXT)
                .choices(BotUtil.getCommandChoices())
                .build();
        userMessageDto.setPayload(payload);

        return userMessageDto;
    }
}
