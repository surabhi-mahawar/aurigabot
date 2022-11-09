package com.dynamos.aurigabot.service;

import com.dynamos.aurigabot.dto.MessagePayloadChoiceDto;
import com.dynamos.aurigabot.dto.MessagePayloadDto;
import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.entity.Flow;
import com.dynamos.aurigabot.entity.LeaveRequest;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.entity.UserMessage;
import com.dynamos.aurigabot.enums.*;
import com.dynamos.aurigabot.repository.FlowRepository;
import com.dynamos.aurigabot.repository.LeaveRequestRepository;
import com.dynamos.aurigabot.repository.UserMessageRepository;
import com.dynamos.aurigabot.repository.UserRepository;
import com.dynamos.aurigabot.response.HttpApiResponse;
import com.dynamos.aurigabot.utils.BotUtil;
import com.dynamos.aurigabot.utils.UserMessageUtil;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public Mono<UserMessageDto> processMessage(List<User> user, UserMessage incomingUserMessage) throws ParseException {
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
        CommandType commandType = isCommand(incomingUserMessage.getMessage());


        LeaveRequest leaveRequest = LeaveRequest.builder().build();
        /**
         * If user is not found, proceed with register telegram chat id of user flow
         */
        if(user.size() == 0 && incomingUserMessage.getChannel()== MessageChannel.TELEGRAM){
            return  userMessageRepository.findAllByToSourceAndStatusOrderBySentAt(incomingUserMessage.getFromSource(),UserMessageStatus.SENT.name()).collectList().map(new Function<List<UserMessage>, Mono<UserMessageDto>>(){
                @Override
                public Mono<UserMessageDto> apply(List<UserMessage> userMessages){
                    /**
                     * If last message sent to user is empty, proceed with registration request flow
                     * Else If last message sent to user does not have flow, or its command type is not /regTelegramUser, proceed with registration request flow
                     * Else proceed with telegram chat id registration process for the message received from user
                     */
                    if(userMessages.size()==0){
                        return processUnregisteredRequest(outUserMessageDto);
                    } else if(userMessages.get(0).getFlow() == null || !userMessages.get(0).getFlow().getCommandType().equals(CommandType.REGTELEGRAMUSER)){
                        return processUnregisteredRequest(outUserMessageDto);
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
                                        return processUnregisteredRequest(outUserMessageDto);
                                    } else{
                                        User user = users.get(0);
                                        user.setTelegramChatId(incomingUserMessage.getFromSource());
                                        return userRepository.save(user).map(new Function<User, UserMessageDto>() {
                                            @Override
                                            public UserMessageDto apply(User user) {
                                                return registeredTelegramUserDto(outUserMessageDto);
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
                            return processInvalidRequest(outUserMessageDto);
                        }
                    }
                }
            }).flatMap(new Function<Mono<UserMessageDto>, Mono<? extends UserMessageDto>>() {
                @Override
                public Mono<? extends UserMessageDto> apply(Mono<UserMessageDto> userMessageDtoMono) {
                    return userMessageDtoMono;
                }
            });
        } else if (isBotStartingMessage(incomingUserMessage.getMessage())) {
            return processBotStartingMessage(user.get(0), outUserMessageDto);
        } else if(commandType != null) {
            return processCommand(user.get(0), outUserMessageDto, commandType,incomingUserMessage);
        } else if(userMessageRepository.findAllByToSourceAndStatusOrderBySentAt(incomingUserMessage.getFromSource(),UserMessageStatus.SENT.name()).collectList().map(new Function<List<UserMessage>, Flow>() {
            @Override
            public Flow apply(List<UserMessage> userMessages) {
                return userMessages.get(0).getFlow();
            }
        })!=null) {
            return processCommand(user.get(0), outUserMessageDto, CommandType.LEAVE,incomingUserMessage);
        }
        else {
            return processInvalidRequest(outUserMessageDto);
        }
    }

    /**
     * Check if message equals bot starting message
     * @param message
     * @return
     */
    public Boolean isBotStartingMessage (String message) {
        return message.trim().equalsIgnoreCase(BotUtil.BOT_START_MSG);
    }

    /**
     * is command
     * @param message
     * @return
     */
    public CommandType isCommand(String message) {
        message = message.trim();

        for (Map<String, String> command : getCommandsList()) {
            if(message.equalsIgnoreCase(command.get("key"))) {
                return CommandType.valueOf(command.get("key"));
            } else if(message.equalsIgnoreCase((command.get("value")))) {
                return CommandType.getEnumByValue(command.get("value"));
            }
        }
        return null;
    }

    /**
     * Get list of commands for bot
     * @return
     */
    public List<Map<String, String>> getCommandsList() {
        List<Map<String, String>> list = new ArrayList<>();
        for (CommandType value : CommandType.values()) {
            if(!value.equals(CommandType.REGTELEGRAMUSER)) {
                Map<String, String> data = new HashMap();
                data.put("key", value.toString());
                data.put("value", value.getDisplayValue());
                list.add(data);
            }
        }
        return list;
    }

    /**
     * Process starting message to return list of commands for bot
     * @return
     */
    public Mono<UserMessageDto> processBotStartingMessage(User user, UserMessageDto userMessageDto) {
        String msgText = "Hello "+user.getName()+", \n Please select a option from the list to proceed further.";
        userMessageDto.setMessage(msgText);

        ArrayList<MessagePayloadChoiceDto> choices = new ArrayList<>();
        getCommandsList().forEach(command -> {
            MessagePayloadChoiceDto choice = MessagePayloadChoiceDto.builder()
                    .key(command.get("key"))
                    .text(command.get("value"))
                    .build();
            choices.add(choice);
        });

        MessagePayloadDto payload = MessagePayloadDto.builder()
                .message(msgText)
                .msgType(MessagePayloadType.TEXT)
                .choices(choices)
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
            return processBirthdayRequest( userMessageDto,"/birthday", 0);
        } else if (commandType.equals(CommandType.LEAVE)) {
            return processLeaveRequest(userMessageDto,"/leave",0,incomingMessageDto,user);
        }
        else {
            return processInvalidRequest(userMessageDto);
        }
    }

    /**
     * Process /birthday command and return list of birthdays for today
     * @param userMessageDto
     * @param commandType
     * @param index
     * @return
     */
    private Mono<UserMessageDto> processBirthdayRequest( UserMessageDto userMessageDto,String commandType, int index) {
//        return flowRepository.findByIndexAndCommandType(index,commandType).map(new Function<Flow, UserMessageDto>() {
//            @Override
//            public UserMessageDto apply(Flow flow) {
//                userMessageDto.setMessage(flow.getQuestion());
//
//                userMessageDto.setFlowId(flow.getId());
//                MessagePayloadDto payload = MessagePayloadDto.builder()
//                        .message(flow.getQuestion())
//                        .msgType(MessagePayloadType.TEXT)
//                        .build();
//
//                userMessageDto.setPayload(payload);
//
//                return userMessageDto;
//
//            }
//        });
        SimpleDateFormat mdyFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date currentDate = java.util.Date.from(Instant.now());
        String dateStr = mdyFormat.format(currentDate);

        java.util.Date dob2 = null;
        try {
            dob2 = mdyFormat.parse(dateStr);

            return userRepository.findAllByDob(dob2).collectList().map(new Function<List<User>, UserMessageDto>() {
                @Override
                public UserMessageDto apply(List<User> users) {

                    String message;
                    if (users.size() == 0){
                        message = "There are no birthdays today";
                    } else {
                        message = "Please find the list of birthdays for today.\n";
                        for (User u : users){
                            message += "\n"+u.getName();
                        }
                    }

                    userMessageDto.setMessage(message);
                    MessagePayloadDto payload = MessagePayloadDto.builder()
                            .message(message)
                            .msgType(MessagePayloadType.TEXT)
                            .build();

                    userMessageDto.setPayload(payload);

                    return userMessageDto;
                }
            });
        } catch (ParseException e) {
//            throw new RuntimeException(e);
            return Mono.just(null);
        }
    }

    private Mono<UserMessageDto> processLeaveRequest( UserMessageDto userMessageDto,String commandType, int index,UserMessage incomingUserMessage,User user) {
        return leaveRequestRepository.findByEmployeeId(user.getId()).collectList().map(new Function<List<LeaveRequest>, LeaveRequest>() {
            @Override
            public  LeaveRequest apply(List<LeaveRequest> leaveRequests) {
                if (leaveRequests.size()==0){
                    return LeaveRequest.builder().build();
                }
                else{
                    return leaveRequests.get(0);
                }
            }
        }).map(
                new Function<LeaveRequest, Mono<UserMessageDto>>() {
                    @Override
                    public Mono<UserMessageDto> apply(LeaveRequest leaveRequest) {
                        return userMessageRepository.findAllByToSourceAndStatusOrderBySentAt(userMessageDto.getToSource(),UserMessageStatus.SENT.name()).collectList().map(new Function<List<UserMessage>,Mono<Mono<UserMessageDto>>>() {
                            @Override
                            public Mono<Mono<UserMessageDto>> apply(List<UserMessage> userMessages) {
                                if (userMessages.get(0).getFlow()==null){
                                    return flowRepository.findByIndexAndCommandType(index, CommandType.LEAVE.getDisplayValue()).collectList().map(new Function<List<Flow>, Mono<UserMessageDto>>() {
                                        @Override
                                        public Mono<UserMessageDto> apply(List<Flow> flow) {
                                            if(flow.size()!=0 ) {
//
                                                userMessageDto.setMessage(flow.get(index).getQuestion());

                                                userMessageDto.setFlow(flow.get(index));
                                                MessagePayloadDto payload = MessagePayloadDto.builder()
                                                        .message(flow.get(0).getQuestion())
                                                        .msgType(MessagePayloadType.TEXT)
                                                        .build();

                                                userMessageDto.setPayload(payload);
                                                return Mono.just(userMessageDto);
                                            } else {
                                                return Mono.just(processInvalidRequestDto(userMessageDto));
                                            }
                                        }
                                    });
                                } else {

                                    if (userMessages.get(0).getIndex()==3){
                                        if(incomingUserMessage.getMessage().equals(LeaveType.CL.getDisplayValue())){
                                            leaveRequest.setLeaveType(LeaveType.CL);
                                        }
                                        else {
                                            leaveRequest.setLeaveType(LeaveType.PL);
                                        }
                                        return leaveRequestRepository.save(leaveRequest).map(new Function<LeaveRequest, Mono<UserMessageDto>>() {
                                            @Override
                                            public Mono<UserMessageDto> apply(LeaveRequest leaveRequest) {
                                                userMessageDto.setMessage("ALL DONE");
                                                return Mono.just(userMessageDto);
                                            }
                                        });

                                    }
                                        return flowRepository.findByIndexAndCommandType(userMessages.get(0).getIndex()+1, CommandType.LEAVE.getDisplayValue()).collectList().map(new Function<List<Flow>, Mono<UserMessageDto>>() {
                                        @Override
                                        public Mono<UserMessageDto> apply(List<Flow> flow) {
                                            if(flow.size()!=0 ) {
                                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                                                leaveRequest.setEmployeeId(user);
                                                leaveRequest.setStatus(LeaveStatus.PENDING);
                                                leaveRequest.setLeaveType(LeaveType.CL);
                                                //todo: change/set approved by
                                                userMessageDto.setMessage(flow.get(0).getQuestion());

                                                userMessageDto.setFlow(flow.get(0));
                                                MessagePayloadDto payload = MessagePayloadDto.builder()
                                                        .message(flow.get(0).getQuestion())
                                                        .msgType(MessagePayloadType.TEXT)
                                                        .build();
                                                userMessageDto.setIndex(flow.get(0).getIndex());
                                                userMessageDto.setPayload(payload);
                                                if (userMessages.get(0).getIndex()==0){
                                                    leaveRequest.setReason(incomingUserMessage.getMessage());
                                                    return leaveRequestRepository.save(leaveRequest).map(new Function<LeaveRequest, UserMessageDto>() {
                                                        @Override
                                                        public UserMessageDto apply(LeaveRequest leaveRequest) {
                                                            return userMessageDto;
                                                        }
                                                    });
                                                }
                                                else if (userMessages.get(0).getIndex()==1){
                                                    leaveRequest.setFromDate(LocalDate.parse(incomingUserMessage.getMessage(), formatter));
                                                    return leaveRequestRepository.save(leaveRequest).map(new Function<LeaveRequest,UserMessageDto>() {
                                                        @Override
                                                        public UserMessageDto apply(LeaveRequest leaveRequest) {
                                                            return userMessageDto;
                                                        }
                                                    });

                                                }
                                                else if (userMessages.get(0).getIndex()==2){
                                                    leaveRequest.setToDate(LocalDate.parse(incomingUserMessage.getMessage(), formatter));
                                                    return leaveRequestRepository.save(leaveRequest).map(new Function<LeaveRequest, UserMessageDto>() {
                                                        @Override
                                                        public UserMessageDto apply(LeaveRequest leaveRequest) {
                                                            return userMessageDto;
                                                        }
                                                    });

                                                }
                                                else if (userMessages.get(0).getIndex()==3){
                                                    if(incomingUserMessage.getMessage().equals(LeaveType.CL.name())){
                                                        leaveRequest.setLeaveType(LeaveType.CL);
                                                    }
                                                    else {
                                                        leaveRequest.setLeaveType(LeaveType.PL);
                                                    }
                                                    return leaveRequestRepository.save(leaveRequest).map(new Function<LeaveRequest, UserMessageDto>() {
                                                        @Override
                                                        public UserMessageDto apply(LeaveRequest leaveRequest) {
                                                            return userMessageDto;
                                                        }
                                                    });

                                                }
                                                else {
                                                    userMessageDto.setMessage("done");
                                                    return Mono.just(userMessageDto);
                                                }
                                            } else {
                                                userMessageDto.setMessage("done");
                                                return Mono.just(userMessageDto);
                                            }
                                        }
                                    });
                                }
                            }
                        }).flatMap(new Function<Mono<Mono<UserMessageDto>>, Mono<? extends UserMessageDto>>() {
                            @Override
                            public Mono<? extends UserMessageDto> apply(Mono<Mono<UserMessageDto>> monoMono) {
                                return monoMono.flatMap(new Function<Mono<UserMessageDto>, Mono<? extends UserMessageDto>>() {
                                    @Override
                                    public Mono<? extends UserMessageDto> apply(Mono<UserMessageDto> userMessageDtoMono) {
                                        return userMessageDtoMono;
                                    }
                                });
                            }
                        });
                    }
                }
        ).flatMap(new Function<Mono<UserMessageDto>, Mono<? extends UserMessageDto>>() {
            @Override
            public Mono<? extends UserMessageDto> apply(Mono<UserMessageDto> userMessageDtoMono) {
                return userMessageDtoMono;
            }
        });



    }


    /**
     * Process unidentified/invalid request
     * @param userMessageDto
     * @return
     */
    private Mono<UserMessageDto> processInvalidRequest(UserMessageDto userMessageDto) {
        return Mono.just(processInvalidRequestDto(userMessageDto));
    }

    private UserMessageDto processInvalidRequestDto(UserMessageDto userMessageDto) {
        String msgText = "We do not understand your request, please try: "+BotUtil.BOT_START_MSG;
        userMessageDto.setMessage(msgText);

        MessagePayloadDto payload = MessagePayloadDto.builder()
                .message(msgText)
                .msgType(MessagePayloadType.TEXT)
                .build();
        userMessageDto.setPayload(payload);

        return userMessageDto;
    }

    private UserMessageDto registeredTelegramUserDto(UserMessageDto userMessageDto) {
        String msgText = "You have been successfully registered, Please select one choice from the list below.";
        userMessageDto.setMessage(msgText);

        ArrayList<MessagePayloadChoiceDto> choices = new ArrayList<>();
        getCommandsList().forEach(command -> {
            MessagePayloadChoiceDto choice = MessagePayloadChoiceDto.builder()
                    .key(command.get("key"))
                    .text(command.get("value"))
                    .build();
            choices.add(choice);
        });

        MessagePayloadDto payload = MessagePayloadDto.builder()
                .message(msgText)
                .msgType(MessagePayloadType.TEXT)
                .choices(choices)
                .build();
        userMessageDto.setPayload(payload);

        return userMessageDto;
    }

    private Mono<UserMessageDto> processUnregisteredRequest(UserMessageDto userMessageDto) {
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
                    return processInvalidRequestDto(userMessageDto);
                }
            }
        });
    }
}
