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
import com.aurigabot.service.calendar_events.CalendarService;
import com.aurigabot.service.calendar_events.GoogleAuthService;
import com.aurigabot.utils.BotUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.function.Function;

@Service
public class EventsService {

    @Autowired
    private FlowRepository flowRepository;
    @Autowired
    private CalendarUserRepository calendarUserRepository;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private GoogleAuthService googleAuthService;



    public Mono<UserMessageDto> processEventsRequest(User user, UserMessage incomingUserMessage, UserMessageDto outUserMessageDto, CommandType commandType, UserMessage lastSentMessage){

        return calendarUserRepository.findByEmail(user.getEmail()).flux().collectList().map(new Function<List<GoogleTokens>, Mono<UserMessageDto>>() {
                    @Override
                    public Mono<UserMessageDto> apply(List<GoogleTokens> tokens) {

                        if(tokens.size() > 0) {
                            googleAuthService.getNewAccessTokenUsingRefreshToken(user.getEmail()).subscribe();
                            if (lastSentMessage != null) {
                                Flow lastFlow = lastSentMessage.getFlow();
                                Integer lastIndex = lastSentMessage.getIndex();
                                if(lastSentMessage.getFlow().getCommandType().equals(CommandType.LISTEVENTS)) {
                                   return processListEvents(lastIndex,lastFlow,user,incomingUserMessage,outUserMessageDto,lastSentMessage);
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

    public Mono<UserMessageDto> processListEvents(int lastIndex, Flow lastFlow, User user, UserMessage incomingUserMessage, UserMessageDto outUserMessageDto, UserMessage lastSentMessage){
        if(lastIndex >= 0 && lastIndex <= 1) {
            Pair<String, Object> result = validationService.fieldValidator(lastFlow.getValidation(),incomingUserMessage.getMessage());
            if(result.getFirst() == "pass") {
                if (lastIndex == 1){
                    JSONParser jsonParser = new JSONParser();
                    Object obj=null;
                    try (FileReader reader = new FileReader(user.getId()+".json"))
                    {
                        obj = jsonParser.parse(reader);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    JSONObject eventList = (JSONObject) obj;
                    return fetchEvents(eventList.get("start_date").toString(),result.getSecond().toString(), user.getEmail(), outUserMessageDto);
                } else {
                    JSONObject eventListJson = new JSONObject();
                    eventListJson.put("start_date",result.getSecond().toString());

                    try (FileWriter file = new FileWriter(user.getId()+".json")) {
                        file.write(eventListJson.toJSONString());
                        file.flush();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return getFlowByIndex(lastIndex+1,lastSentMessage.getFlow().getCommandType()).collectList()
                            .map(new Function<List<Flow>, UserMessageDto>() {
                                @Override
                                public UserMessageDto apply(List<Flow> flows) {
                                    if(flows.size() > 0 && flows.get(0) != null) {
                                        Flow flow=flows.get(0);
                                        outUserMessageDto.setMessage(flow.getQuestion());
                                        outUserMessageDto.setFlow(flow);
                                        outUserMessageDto.setIndex(flow.getIndex());

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
    }

    public Mono<UserMessageDto> fetchEvents(String startDate, String endDate, String email, UserMessageDto outUserMessageDto){
        return calendarService.fetchCalendarEvents(startDate,endDate, email,null).
                map(u -> {
                    outUserMessageDto.setMessage(u);
                    outUserMessageDto.setFlow(null);
                    outUserMessageDto.setIndex(0);

                    MessagePayloadDto payload = MessagePayloadDto.builder()
                            .message("finalResult")
                            .msgType(MessagePayloadType.TEXT)
                            .choices(BotUtil.getCommandChoices())
                            .build();
                    outUserMessageDto.setPayload(payload);
                    return Mono.just(outUserMessageDto);
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
