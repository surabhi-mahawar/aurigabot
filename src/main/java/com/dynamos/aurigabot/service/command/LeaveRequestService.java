package com.dynamos.aurigabot.service.command;

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
import com.dynamos.aurigabot.utils.BotUtil;
import lombok.Builder;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

@Builder
public class LeaveRequestService {
    private UserMessageRepository userMessageRepository;
    private FlowRepository flowRepository;
    private LeaveRequestRepository leaveRequestRepository;

    /**
     * Process leave request flow
     * @param user
     * @param incomingUserMessage
     * @param outUserMessageDto
     * @param lastSentMessage
     * @return
     */
    public Mono<UserMessageDto> processApplyLeaveRequest(User user, UserMessage incomingUserMessage, UserMessageDto outUserMessageDto, UserMessage lastSentMessage) {
        if(lastSentMessage != null && lastSentMessage.getFlow().getCommandType().equals(CommandType.LEAVE)) {
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
                                    result = processLeaveType(leaveRequest, outUserMessageDto, incomingUserMessage, lastFlow);
                                }

                                return result.map(new Function<Pair<Boolean, String>, Mono<UserMessageDto>>() {
                                    @Override
                                    public Mono<UserMessageDto> apply(Pair<Boolean, String> resultPait) {
                                        /**
                                         * If the result is true, process for next reply
                                         * Else send error messsage from result pair as reply
                                         */
                                        if(resultPait.getFirst().booleanValue() == true) {
                                            /**
                                             * If result pair message is not empty & is last index, send this as reply
                                             * Else find the next flow question and send it as reply
                                             */
                                            if(!resultPait.getSecond().isEmpty() && lastIndex == 3) {
                                                outUserMessageDto.setMessage(resultPait.getSecond());
                                                outUserMessageDto.setFlow(null);
                                                outUserMessageDto.setIndex(0);

                                                MessagePayloadDto payload = MessagePayloadDto.builder()
                                                        .message(resultPait.getSecond())
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
                                            outUserMessageDto.setMessage(resultPait.getSecond());
                                            outUserMessageDto.setFlow(lastFlow);
                                            outUserMessageDto.setIndex(lastIndex);

                                            MessagePayloadDto payload = MessagePayloadDto.builder()
                                                    .message(resultPait.getSecond())
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
        return flowRepository.findByIndexAndCommandType(index, CommandType.LEAVE.getDisplayValue());
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
        Boolean valid = true;
        String errorMsg = "";
        Pattern p = Pattern.compile("^[a-zA-Z0-9! ]*$");
        boolean str = p.matcher(incomingUserMessage.getMessage()).matches();
        if (str){
            leaveRequest.setReason(incomingUserMessage.getMessage());
        } else{
            valid = false;
            errorMsg = "please use only alphabets and numbers";
        }
        if(valid) {
            return leaveRequestRepository.save(leaveRequest).map(new Function<LeaveRequest, Pair<Boolean, String>>() {
                @Override
                public Pair<Boolean, String> apply(LeaveRequest leaveRequest) {
                    return Pair.of(true, "");
                }
            });
        } else {
            return Mono.just(Pair.of(valid, errorMsg));
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
        Boolean valid = true;
        String errorMsg = "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        try {
            LocalDate date = LocalDate.parse(incomingUserMessage.getMessage(), formatter);
            if(date.compareTo(LocalDate.now()) < 0) {
                valid = false;
                errorMsg = "Entered date should be greator than or equal to current date.";
            } else {
                if (index == 1) {
                    leaveRequest.setFromDate(date);
                } else {
                    leaveRequest.setToDate(date);
                    if(leaveRequest.getToDate().compareTo(leaveRequest.getFromDate()) < 0) {
                        valid = false;
                        errorMsg = "To date should be greator than or equal to from date.";
                    }
                }
            }
        } catch (DateTimeParseException e){
            valid = false;
            errorMsg = "please enter the date in proper format i.e dd-mm-yyyy eg. 11-02-2000";
        }
        if(valid) {
            return leaveRequestRepository.save(leaveRequest).map(new Function<LeaveRequest, Pair<Boolean, String>>() {
                @Override
                public Pair<Boolean, String> apply(LeaveRequest leaveRequest) {
                    return Pair.of(true, "");
                }
            });
        } else {
            return Mono.just(Pair.of(valid, errorMsg));
        }
    }

    /**
     * Process leave type question's answer with validation
     * @param leaveRequest
     * @param userMessageDto
     * @param incomingUserMessage
     * @return
     */
    private Mono<Pair<Boolean, String>> processLeaveType(LeaveRequest leaveRequest, UserMessageDto userMessageDto, UserMessage incomingUserMessage, Flow flow) {
        Boolean valid = true;
        String errorMsg = "";
        if(incomingUserMessage.getMessage().equals(LeaveType.CL.getDisplayValue())){
            leaveRequest.setLeaveType(LeaveType.CL);
        } else if (incomingUserMessage.getMessage().equals(LeaveType.PL.getDisplayValue())){
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
}
