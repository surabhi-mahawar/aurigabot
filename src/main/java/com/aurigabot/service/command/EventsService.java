package com.aurigabot.service.command;

import com.aurigabot.dto.CreateEventRequestDTO;
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
import com.aurigabot.utils.DateUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
                                    return processCreateEvent(lastIndex,lastFlow,user,incomingUserMessage,outUserMessageDto,lastSentMessage);
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

                    if(CompareStartAndEndDate(eventList.get("start_date").toString(), incomingUserMessage.getMessage(),lastFlow.getValidation().getDateValidationConfig().getFormat())) {
                        return fetchEvents(eventList.get("start_date").toString(), result.getSecond().toString(), user.getEmail(), outUserMessageDto);
                    }
                    else{
                        String msg= "Try again !! \nend date should be greater than or equal to start date i.e "+eventList.get("start_date").toString();
                        outUserMessageDto.setMessage(msg);
                        outUserMessageDto.setFlow(lastFlow);
                        outUserMessageDto.setIndex(lastIndex);

                        MessagePayloadDto payload = MessagePayloadDto.builder()
                                .message(msg)
                                .msgType(MessagePayloadType.TEXT)
                                .build();
                        outUserMessageDto.setPayload(payload);
                        return Mono.just(outUserMessageDto);
                    }
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
        return calendarService.fetchCalendarEvents(startDate,endDate, email,null)
                .map(msg -> {
                    outUserMessageDto.setMessage(msg);
                    outUserMessageDto.setFlow(null);
                    outUserMessageDto.setIndex(0);

                    MessagePayloadDto payload = MessagePayloadDto.builder()
                            .message(msg)
                            .msgType(MessagePayloadType.TEXT)
                            .choices(BotUtil.getCommandChoices())
                            .build();
                    outUserMessageDto.setPayload(payload);
                    return outUserMessageDto;
                });
    }

    public Mono<UserMessageDto> processCreateEvent(int lastIndex, Flow lastFlow, User user, UserMessage incomingUserMessage, UserMessageDto outUserMessageDto, UserMessage lastSentMessage){
        if(lastIndex >= 0 && lastIndex <= 5) {
            Pair<String, Object> result = validationService.fieldValidator(lastFlow.getValidation(),incomingUserMessage.getMessage());
            if(result.getFirst() == "pass") {
                JSONObject createEventJson = new JSONObject();
                if (lastIndex == 5){
                    JSONParser jsonParser = new JSONParser();
                    Object obj=null;
                    try (FileReader reader = new FileReader(user.getId()+".json"))
                    {
                        obj = jsonParser.parse(reader);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    createEventJson = (JSONObject) obj;

                    CreateEventRequestDTO createEventRequestDTO = new CreateEventRequestDTO();
                    createEventRequestDTO.setSummary(createEventJson.get("summary").toString());
                    createEventRequestDTO.setLocation(createEventJson.get("location").toString());
                    createEventRequestDTO.setDescription(createEventJson.get("description").toString());
                    createEventRequestDTO.setStartDate(createEventJson.get("start_date").toString());
                    createEventRequestDTO.setEndDate(createEventJson.get("end_date").toString());
                    createEventRequestDTO.setTimezone(result.getSecond().toString());

                    return createEvent(createEventRequestDTO, user.getEmail(), outUserMessageDto);
                } else {
//                    JSONObject createEventJson = new JSONObject();
                    if (lastIndex==0) {
                        createEventJson.put("summary", result.getSecond().toString());
                    } else {
                        JSONParser jsonParser = new JSONParser();
                        Object obj=null;
                        try (FileReader reader = new FileReader(user.getId()+".json"))
                        {
                            obj = jsonParser.parse(reader);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        createEventJson = (JSONObject) obj;
                        switch (lastIndex){
                            case 1:
                                createEventJson.put("location", result.getSecond().toString());break;
                            case 2:
                                createEventJson.put("description", result.getSecond().toString());break;
                            case 3:
                                createEventJson.put("start_date", result.getSecond().toString());break;
                            case 4:
                                createEventJson.put("end_date", result.getSecond().toString());break;
                        }
                    }
                    try (FileWriter file = new FileWriter(user.getId()+".json")) {
                        file.write(createEventJson.toJSONString());
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

    public Mono<UserMessageDto> createEvent(CreateEventRequestDTO createEventRequestDTO, String email, UserMessageDto outUserMessageDto){
        return calendarService.createGoogleCalendarEvent(createEventRequestDTO, email)
        .map(msg -> {
            outUserMessageDto.setMessage(msg);
            outUserMessageDto.setFlow(null);
            outUserMessageDto.setIndex(0);

            MessagePayloadDto payload = MessagePayloadDto.builder()
                    .message(msg)
                    .msgType(MessagePayloadType.TEXT)
                    .choices(BotUtil.getCommandChoices())
                    .build();
            outUserMessageDto.setPayload(payload);
            return outUserMessageDto;
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

    private Boolean CompareStartAndEndDate(String startDateStr, String endDateStr, String format) {
        if (format.equals("dd-MM-yyyy")) {
            LocalDate startDate = null;
            LocalDate endDate = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            try {
                startDate = LocalDate.parse(startDateStr, formatter1);
                startDate = LocalDate.parse(startDate.format(formatter), formatter);
                endDate = LocalDate.parse(endDateStr, formatter);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (endDate.compareTo(startDate) < 0)
                return false;
            else
                return true;
        } else {
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            try {
                startDate = LocalDateTime.parse(startDateStr, formatter1);
                startDate = LocalDateTime.parse(startDate.format(formatter), formatter);
                endDate = LocalDateTime.parse(endDateStr, formatter);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (endDate.compareTo(startDate) < 0)
                return false;
            else
                return true;
        }
    }
}
