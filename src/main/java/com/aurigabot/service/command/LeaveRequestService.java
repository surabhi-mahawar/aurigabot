package com.aurigabot.service.command;

import com.aurigabot.entity.*;
import com.aurigabot.enums.CommandType;
import com.aurigabot.enums.LeaveStatus;
import com.aurigabot.enums.LeaveType;
import com.aurigabot.enums.MessagePayloadType;
import com.aurigabot.repository.*;
import com.aurigabot.service.KafkaProducerService;
import com.aurigabot.service.ValidationService;
import com.aurigabot.utils.BotUtil;
import com.aurigabot.dto.MessagePayloadDto;
import com.aurigabot.dto.UserMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

@Service
public class LeaveRequestService {
    @Autowired
    private UserMessageRepository userMessageRepository;

    @Autowired
    private EmployeeManagerRepository employeeManagerRepository;
@Autowired
    private ValidationService validationService;

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    @Value(value = "${kafka.topic.outbound.message}")
    private String outboundTopic;
    @Autowired
    private UserRepository userRepository;

    /**
     * Process leave request flow
     * @param user
     * @param incomingUserMessage
     * @param outUserMessageDto
     * @param lastSentMessage
     * @return
     */
    public Mono<UserMessageDto> processApplyLeaveRequest(User user, UserMessage incomingUserMessage, UserMessageDto outUserMessageDto, UserMessage lastSentMessage) {
        if(lastSentMessage != null && lastSentMessage.getFlow().getCommandType().equals(CommandType.LEAVEREQUEST)) {
            Flow lastFlow = lastSentMessage.getFlow();
            Integer lastIndex = lastSentMessage.getIndex();
            Integer currentIndex = lastSentMessage.getIndex()+1;
            /* Check if leave request object exists */
            return leaveRequestRepository.findByEmployeeIdAndStatus(user.getId(), LeaveStatus.INACTIVE.toString())
                    .collectList()
                    .map(new Function<List<LeaveRequest>, Mono<UserMessageDto>>() {
                        @Override
                        public Mono<UserMessageDto> apply(List<LeaveRequest> leaveRequests) {
                            /* Leave request object */
                           LeaveRequest leaveRequest = null;
                            if (leaveRequests.size() > 0 && leaveRequests.get(0) != null){
                                leaveRequest = leaveRequests.get(0);
                            } else{
                                leaveRequest = LeaveRequest.builder()
                                        .employeeId(user)
                                        .status(LeaveStatus.INACTIVE)
                                        .leaveType(LeaveType.CL)
                                        .build();
                            }

                            /**
                             * if index from last message is between 0 to 3,
                             * process it further,
                             * else send invalid message
                             */
                            if(lastIndex >= 0 && lastIndex <= 3) {
                                Mono<Pair<Boolean, String>> result = null;
                                if (lastIndex == 0){
                                    result = processLeaveReason(leaveRequest, outUserMessageDto, incomingUserMessage, lastFlow);
                                } else if (lastIndex == 1){
                                    result = processLeaveDates(1,leaveRequest, outUserMessageDto, incomingUserMessage, lastFlow);
                                } else if (lastIndex == 2){
                                    result = processLeaveDates(2,leaveRequest, outUserMessageDto, incomingUserMessage, lastFlow);
                                } else {
                                    result = processLeaveType(user,leaveRequest, outUserMessageDto, incomingUserMessage, lastFlow);
                                }
                                return result.map(new Function<Pair<Boolean, String>, Mono<UserMessageDto>>() {
                                    @Override
                                    public Mono<UserMessageDto> apply(Pair<Boolean, String> resultPair) {
                                        /**
                                         * If the result is true, process for next reply
                                         * Else send error message from result pair as reply
                                         */
                                        if(resultPair.getFirst().booleanValue() == true) {
                                            /**
                                             * If result pair message is not empty & is last index, send this as reply
                                             * Else find the next flow question and send it as reply
                                             */
                                            if(!resultPair.getSecond().isEmpty() && lastIndex == 3) {
                                                outUserMessageDto.setMessage(resultPair.getSecond());
                                                outUserMessageDto.setFlow(null);
                                                outUserMessageDto.setIndex(0);

                                                MessagePayloadDto payload = MessagePayloadDto.builder()
                                                        .message(resultPair.getSecond())
                                                        .msgType(MessagePayloadType.TEXT)
                                                        .build();
                                                outUserMessageDto.setPayload(payload);
                                                return Mono.just(outUserMessageDto);
                                            } else {
                                                return getFlowByIndex(lastIndex+1).collectList()
                                                        .map(new Function<List<Flow>, UserMessageDto>() {
                                                            @Override
                                                            public UserMessageDto apply(List<Flow> flows) {
                                                                if(flows.size() > 0 && flows.get(0) != null) {
                                                                    outUserMessageDto.setMessage(flows.get(0).getQuestion());
                                                                    outUserMessageDto.setFlow(flows.get(0));
                                                                    outUserMessageDto.setIndex(flows.get(0).getIndex());

                                                                    MessagePayloadDto payload = MessagePayloadDto.builder()
                                                                            .message(flows.get(0).getQuestion())
                                                                            .msgType(MessagePayloadType.TEXT)
                                                                            .build();
                                                                    outUserMessageDto.setPayload(payload);
                                                                    return outUserMessageDto;
                                                                } else {
                                                                    return BotUtil.getInvalidRequestMessageDto(outUserMessageDto);
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            outUserMessageDto.setMessage(resultPair.getSecond());
                                            outUserMessageDto.setFlow(lastFlow);
                                            outUserMessageDto.setIndex(lastIndex);

                                            MessagePayloadDto payload = MessagePayloadDto.builder()
                                                    .message(resultPair.getSecond())
                                                    .msgType(MessagePayloadType.TEXT)
                                                    .build();
                                            outUserMessageDto.setPayload(payload);
                                            return Mono.just(outUserMessageDto);
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
                    }).flatMap(new Function<Mono<UserMessageDto>, Mono<? extends UserMessageDto>>() {
                        @Override
                        public Mono<? extends UserMessageDto> apply(Mono<UserMessageDto> userMessageDtoMono) {
                            return userMessageDtoMono;
                        }
                    });
        } else {
            /**
             * Find first flow question and send it as reply
             */
            return getFlowByIndex(0).collectList().map(new Function<List<Flow>, UserMessageDto>() {
                @Override
                public UserMessageDto apply(List<Flow> flows) {
                    if(flows.size() > 0 && flows.get(0) != null) {
                        Flow flow = flows.get(0);
                        outUserMessageDto.setMessage(flow.getQuestion());

                        outUserMessageDto.setFlow(flow);
                        MessagePayloadDto payload = MessagePayloadDto.builder()
                                .message(flow.getQuestion())
                                .msgType(MessagePayloadType.TEXT)
                                .build();

                        outUserMessageDto.setPayload(payload);
                        return outUserMessageDto;
                    } else {
                        return BotUtil.getInvalidRequestMessageDto(outUserMessageDto);
                    }
                }
            });
        }
    }

    /**
     * Get Flow by index
     * @param index
     * @return
     */
    private Flux<Flow> getFlowByIndex(Integer index) {
        return flowRepository.findByIndexAndCommandType(index, CommandType.LEAVEREQUEST.getDisplayValue());
    }

    /**
     * Process leave reason question's answer with validation
     * @param leaveRequest
     * @param userMessageDto
     * @param incomingUserMessage
     * @param flow
     * @return
     */
    private Mono<Pair<Boolean, String>> processLeaveReason(LeaveRequest leaveRequest, UserMessageDto userMessageDto, UserMessage incomingUserMessage, Flow flow) {
        Boolean valid;
        String result = validationService.fieldValidator(flow.getValidation(),incomingUserMessage.getMessage()).getFirst();
        if (result.equals("pass")){
            valid=true;
            leaveRequest.setReason(incomingUserMessage.getMessage());
        } else{
            valid = false;
        }
        if(valid) {
            return leaveRequestRepository.save(leaveRequest).map(new Function<LeaveRequest, Pair<Boolean, String>>() {
                @Override
                public Pair<Boolean, String> apply(LeaveRequest leaveRequest) {
                    return Pair.of(true, result);
                }
            });
        } else {
            return Mono.just(Pair.of(valid, result));
        }
    }

    /**
     * Process leave dates question's answer with validation
     * @param index
     * @param leaveRequest
     * @param userMessageDto
     * @param incomingUserMessage
     * @param flow
     * @return
     */
    private Mono<Pair<Boolean, String>> processLeaveDates(Integer index, LeaveRequest leaveRequest, UserMessageDto userMessageDto, UserMessage incomingUserMessage, Flow flow) {
        Pair<String,Object> valid = validationService.fieldValidator(flow.getValidation(),incomingUserMessage.getMessage());
        String result = valid.getFirst();
//        LocalDate date = (LocalDate) valid.getSecond();
        if(result=="pass") {
            LocalDate date = (LocalDate) valid.getSecond();
                if (index == 1) {
                    leaveRequest.setFromDate(date);
                    result="pass";
                } else {
                    leaveRequest.setToDate(date);
                    if(leaveRequest.getToDate().compareTo(leaveRequest.getFromDate()) < 0) {
                        result = "Try again !! \nTo date should be greater than or equal to from date i.e "+leaveRequest.getFromDate();

                        return Mono.just(Pair.of(false, result));
                    }
                    else {
                        result="pass";
                    }
                }
//            }
            return leaveRequestRepository.save(leaveRequest).map(new Function<LeaveRequest, Pair<Boolean, String>>() {
                @Override
                public Pair<Boolean, String> apply(LeaveRequest leaveRequest) {
                    return Pair.of(true, "");
                }
            });
        } else {
            return Mono.just(Pair.of(false, result));
        }
    }

    /**
     * Process leave type question's answer with validation
     * @param leaveRequest
     * @param userMessageDto
     * @param incomingUserMessage
     * @return
     */
    private Mono<Pair<Boolean, String>> processLeaveType(User user,LeaveRequest leaveRequest, UserMessageDto userMessageDto, UserMessage incomingUserMessage, Flow flow) {
        Boolean valid = true;
        String errorMsg = "";
        if(incomingUserMessage.getMessage().toUpperCase().equals(LeaveType.CL.getDisplayValue())){
            leaveRequest.setLeaveType(LeaveType.CL);
        } else if (incomingUserMessage.getMessage().toUpperCase().equals(LeaveType.PL.getDisplayValue())){
            leaveRequest.setLeaveType(LeaveType.PL);
        } else {
            valid = false;
            errorMsg = "please enter valid types eg. "+LeaveType.CL.getDisplayValue()+"/"+LeaveType.PL.getDisplayValue();
        }
        if(valid) {
            leaveRequest.setStatus(LeaveStatus.PENDING);
            return leaveRequestRepository.save(leaveRequest).map(new Function<LeaveRequest, Pair<Boolean, String>>() {
                @Override
                public Pair<Boolean, String> apply(LeaveRequest leaveRequest) {
                    return Pair.of(true, "Your leave request has been submitted. Thanks!");
                }
            });
        } else {
            return Mono.just(Pair.of(valid, errorMsg));
        }
    }

    public Mono<Boolean> notifyManager(UserMessageDto outUserMessageDto) {

        ObjectMapper Obj = new ObjectMapper();
        UserMessageDto  managerMessage = Obj.convertValue(outUserMessageDto,UserMessageDto.class);

        return userRepository.findByTelegramChatId(outUserMessageDto.getToSource()).collectList().map(new Function<List<User>, Mono<Mono<Boolean>>>() {
            @Override
            public Mono<Mono<Boolean>> apply(List<User> users) {
                return employeeManagerRepository.findByEmployee(users.get(0).getId()).collectList().map(new Function<List<EmployeeManager>, Mono<Boolean>>() {
                    @Override
                    public Mono<Boolean> apply(List<EmployeeManager> employeeManagers) {

                        return leaveRequestRepository.findByEmployeeIdAndStatus(users.get(0).getId(),LeaveStatus.PENDING.name()).collectList().flatMap(new Function<List<LeaveRequest>, Mono<Boolean>>() {
                            @Override
                            public Mono<Boolean> apply(List<LeaveRequest> leaveRequests) {
                                for (EmployeeManager em : employeeManagers) {
                                    String msgText= "<b>"+users.get(0).getName()+"</b>" + " has applied for leave\nfrom " + "<b>"+leaveRequests.get(0).getFromDate() +"</b>" + " to " + "<b>"+leaveRequests.get(0).getToDate()+"</b>"+"\n<b>Reason:- </b>"+leaveRequests.get(0).getReason()+"\n<b>Leave Type:- </b>"+leaveRequests.get(0).getLeaveType().getDisplayValue() + "\nplease reply with <b>Approve or Reject </b>";
                                    managerMessage.setToSource(em.getManager().getTelegramChatId());
                                    managerMessage.setMessage(msgText);
                                    managerMessage.setToUserId(em.getManager().getId());

                                    MessagePayloadDto payload = MessagePayloadDto.builder()
                                            .message(msgText)
                                            .msgType(MessagePayloadType.TEXT)
                                            .choices(BotUtil.getLeaveCommandChoices())
                                            .build();

                                    managerMessage.setPayload(payload);



                                    try {
                                        String jsonStr = Obj.writeValueAsString(managerMessage);
                                        kafkaProducerService.sendMessage(jsonStr, outboundTopic);
                                    } catch (JsonProcessingException e) {
//                        return Mono.just(false);
                                    }
                                }
                                return Mono.just(true);
                            }
                        });
                            }
                        });

            }
        }).flatMap(new Function<Mono<Mono<Boolean>>, Mono<? extends Boolean>>() {
            @Override
            public Mono<? extends Boolean> apply(Mono<Mono<Boolean>> monoMono) {
                return monoMono.flatMap(new Function<Mono<Boolean>, Mono<? extends Boolean>>() {
                    @Override
                    public Mono<? extends Boolean> apply(Mono<Boolean> booleanMono) {
                        return booleanMono;
                    }
                });
            }
        });

    }
    public Mono<UserMessageDto> handleLeaveRequest(User user, UserMessageDto outUserMessageDto,CommandType commandType) {
        ObjectMapper Obj = new ObjectMapper();
        UserMessageDto  employeeMessage = Obj.convertValue(outUserMessageDto,UserMessageDto.class);
return employeeManagerRepository.findByManager(user.getId()).collectList().map(new Function<List<EmployeeManager>, UserMessageDto>() {
    @Override
    public UserMessageDto apply(List<EmployeeManager> employeeManagers) {
        employeeManagers.forEach((em)->{
            String msg ="Your leave request has been <b>";
            employeeMessage.setToUserId(em.getEmployee().getId());
            employeeMessage.setToSource(em.getEmployee().getTelegramChatId());
           msg = commandType.equals(CommandType.APPROVE)?msg+"approved":msg+"rejected";
            employeeMessage.setMessage(msg+"</b>");
            try {
                String jsonStr = Obj.writeValueAsString(employeeMessage);
                kafkaProducerService.sendMessage(jsonStr, outboundTopic);
            } catch (JsonProcessingException e) {
//                        return Mono.just(false);
            }
        });
        outUserMessageDto.setMessage("Thank-you");
        return outUserMessageDto;
    }
});
    }

//    public Mono<UserMessageDto> rejectLeaveRequest(User user, UserMessageDto outUserMessageDto) {
//        ObjectMapper Obj = new ObjectMapper();
//        UserMessageDto  employeeMessage = Obj.convertValue(outUserMessageDto,UserMessageDto.class);
//        return employeeManagerRepository.findByManager(user.getId()).collectList().map(new Function<List<EmployeeManager>, UserMessageDto>() {
//            @Override
//            public UserMessageDto apply(List<EmployeeManager> employeeManagers) {
//                employeeManagers.forEach((em)->{
//                    employeeMessage.setToUserId(em.getEmployee().getId());
//                    employeeMessage.setToSource(em.getEmployee().getTelegramChatId());
//                    employeeMessage.setMessage("Your leave request has been Rejected");
//                    try {
//                        String jsonStr = Obj.writeValueAsString(employeeMessage);
//                        kafkaProducerService.sendMessage(jsonStr, outboundTopic);
//                    } catch (JsonProcessingException e) {
////                        return Mono.just(false);
//                    }
//                });
//                outUserMessageDto.setMessage("Thank-you");
//                return outUserMessageDto;
//            }
//        });
//    }
//
}
