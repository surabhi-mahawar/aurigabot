package com.aurigabot.service.command;

import com.aurigabot.dto.MessagePayloadDto;
import com.aurigabot.dto.UserMessageDto;
import com.aurigabot.entity.Flow;
import com.aurigabot.entity.GoogleTokens;
import com.aurigabot.entity.User;
import com.aurigabot.entity.UserMessage;
import com.aurigabot.enums.CommandType;
import com.aurigabot.enums.MessagePayloadType;
import com.aurigabot.repository.CalendarUserRepository;
import com.aurigabot.repository.FlowRepository;
import com.aurigabot.service.ValidationService;
import com.aurigabot.service.calendar_events.ListEventsService;
import com.aurigabot.utils.BotUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Service
public class EventsService {

    @Autowired
    private FlowRepository flowRepository;
    @Autowired
    private CalendarUserRepository calendarUserRepository;
    @Autowired
    private ListEventsService listEventsService;
    @Autowired
    private ValidationService validationService;



    public Mono<UserMessageDto> processEventsRequest(User user, UserMessage incomingUserMessage, UserMessageDto outUserMessageDto, CommandType commandType, UserMessage lastSentMessage){

        return calendarUserRepository.findByEmail(user.getEmail()).flux().collectList().map(new Function<List<GoogleTokens>, Mono<UserMessageDto>>() {
            @Override
            public Mono<UserMessageDto> apply(List<GoogleTokens> tokens) {
                if(tokens.size() > 0) {
                    if (lastSentMessage != null) {
                        Flow lastFlow = lastSentMessage.getFlow();
                        Integer lastIndex = lastSentMessage.getIndex();
                        Integer currentIndex = lastSentMessage.getIndex() + 1;
                        if(lastSentMessage.getFlow().getCommandType().equals(CommandType.LISTEVENTS)) {
                            if(lastIndex >= 0 && lastIndex <= 1) {
                                Pair<String, Object> result = validationService.fieldValidator(lastFlow.getValidation(),incomingUserMessage.getMessage());
                                if(result.getFirst() == "pass") {
                                    if (lastIndex == 1){

                                    } else {

                                    }
                                }
                                else {
                                    outUserMessageDto.setMessage(result.getFirst());
                                    outUserMessageDto.setFlow(lastFlow);
                                    outUserMessageDto.setIndex(lastIndex);

                                    MessagePayloadDto payload = MessagePayloadDto.builder()
                                            .message(result.getFirst())
                                            .msgType(MessagePayloadType.TEXT)
                                            .build();
                                    outUserMessageDto.setPayload(payload);
                                    return Mono.just(outUserMessageDto);
                                }
                            } else {
                                return Mono.just(BotUtil.getInvalidRequestMessageDto(outUserMessageDto));
                            }
//                            listEventsService.fetchEvents(tokens);
                        }
                        else if(lastSentMessage.getFlow().getCommandType().equals(CommandType.CREATEEVENT)) {
                        }

                    } else {
                        /**
                         * Find first flow question and send it as reply
                         */
                        return getFlowByIndex(0, commandType).collectList().map(new Function<List<Flow>, UserMessageDto>() {
                                @Override
                                public UserMessageDto apply(List<Flow> flows) {
                                    if (flows.size() > 0 && flows.get(0) != null) {
                                        Flow flow = flows.get(0);
                                        outUserMessageDto.setMessage(flow.getQuestion());

                                        outUserMessageDto.setFlow(flow);
                                        MessagePayloadDto payload = MessagePayloadDto.builder()
                                                .message(flow.getQuestion())
                                                .msgType(MessagePayloadType.TEXT)
                                                .build();
                                        if(commandType.equals(CommandType.EVENTS)){
                                            payload.setChoices(BotUtil.getEventCommandChoices());
                                        }

                                        outUserMessageDto.setPayload(payload);
                                        return outUserMessageDto;
                                    } else {
                                        return BotUtil.getInvalidRequestMessageDto(outUserMessageDto);
                                    }
                                }
                            });
                    }
                    return Mono.just(outUserMessageDto);
                } else {
                    return Mono.just(testGoogleAccess(outUserMessageDto));
                }

            }
        })
                .flatMap(new Function<Mono<UserMessageDto>, Mono<? extends UserMessageDto>>() {
                    @Override
                    public Mono<? extends UserMessageDto> apply(Mono<UserMessageDto> userMessageDtoMono) {
                        return userMessageDtoMono;
                    }
                });

    }

    private Flux<Flow> getFlowByIndex(Integer index, CommandType commandType) {
        return flowRepository.findByIndexAndCommandType(index, commandType.getDisplayValue());
    }

    private UserMessageDto testGoogleAccess(UserMessageDto outUserMessageDto){
        outUserMessageDto.setMessage("Please Authorize yourself on this link <a href=\"http://127.0.0.1:8081/oauth\">Click here</a>");

        MessagePayloadDto payload = MessagePayloadDto.builder()
                .message(outUserMessageDto.getMessage())
                .msgType(MessagePayloadType.TEXT)
                .build();

        outUserMessageDto.setPayload(payload);
        return outUserMessageDto;
    }

}
