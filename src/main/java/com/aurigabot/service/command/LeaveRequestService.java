package com.aurigabot.service.command;

import com.aurigabot.entity.Flow;
import com.aurigabot.entity.LeaveRequest;
import com.aurigabot.entity.User;
import com.aurigabot.entity.UserMessage;
import com.aurigabot.enums.CommandType;
import com.aurigabot.enums.LeaveStatus;
import com.aurigabot.enums.LeaveType;
import com.aurigabot.enums.MessagePayloadType;
import com.aurigabot.repository.FlowRepository;
import com.aurigabot.repository.LeaveRequestRepository;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.repository.UserRepository;
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

                                LeaveRequest finalLeaveRequest = leaveRequest;
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
                                                Mono<Boolean> sent =  approveLeaveRequest(user, outUserMessageDto,finalLeaveRequest,lastFlow);
                                                return sent.map(new Function<Boolean, UserMessageDto>() {
                                                    @Override
                                                    public UserMessageDto apply(Boolean aBoolean) {
                                                        if (aBoolean != true) {
                                                            outUserMessageDto.setMessage("Something went wrong");
                                                        }
                                                        return outUserMessageDto;
                                                    }
                                                });
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
        String result = validationService.fieldValidator(flow.getValidation(),incomingUserMessage.getMessage(),null,null).getFirst();
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
        Pair<String,Object> valid = validationService.fieldValidator(flow.getValidation(),incomingUserMessage.getMessage(),index,leaveRequest);
        String result = valid.getFirst();
        LocalDate date = (LocalDate) valid.getSecond();
        if(result=="pass") {
//            LocalDate date = (LocalDate) valid.getSecond();
//            if(date.compareTo(LocalDate.now()) < 0) {
//                result = "Try again !! \nEntered date should be greater than or equal to current date.";
//            } else {
                if (index == 1) {
                    leaveRequest.setFromDate(date);
                    result="pass";
                } else {
                    leaveRequest.setToDate(date);
                    if(leaveRequest.getToDate().compareTo(leaveRequest.getFromDate()) < 0) {
                        result = "Try again !! \nTo date should be greater than or equal to from date i.e "+leaveRequest.getFromDate();
                        Mono.just(Pair.of(false, result));
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

    public Mono<Boolean> approveLeaveRequest(User user,UserMessageDto outUserMessageDto,LeaveRequest leaveRequest, Flow flow) {
        ObjectMapper Obj = new ObjectMapper();
        UserMessageDto  managerMessage = Obj.convertValue(outUserMessageDto,UserMessageDto.class);

       return userRepository.findById(user.getManagerId()).map(new Function<User, Boolean>() {
            @Override
            public Boolean apply(User manager) {
                managerMessage.setToSource(manager.getTelegramChatId());
                managerMessage.setMessage(user.getName()+ " has applied for leave from "+ leaveRequest.getFromDate() + " to " + leaveRequest.getToDate()+ " please reply with approve or reject ");
                managerMessage.setToUserId(manager.getId());
                try {
                    String jsonStr = Obj.writeValueAsString(managerMessage);
                    kafkaProducerService.sendMessage(jsonStr,outboundTopic);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        });
    }

}
