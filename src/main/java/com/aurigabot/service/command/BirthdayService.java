package com.aurigabot.service.command;

import com.aurigabot.entity.Flow;
import com.aurigabot.entity.User;
import com.aurigabot.entity.UserMessage;
import com.aurigabot.enums.CommandType;
import com.aurigabot.repository.FlowRepository;
import com.aurigabot.repository.UserRepository;
import com.aurigabot.dto.MessagePayloadDto;
import com.aurigabot.dto.UserMessageDto;
import com.aurigabot.enums.MessagePayloadType;
import com.aurigabot.utils.BotUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class BirthdayService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlowRepository flowRepository;

    /**
     * Process /birthday command and return list of birthdays for today
     * @param userMessageDto
     * @param commandType
     * @param index
     * @return
     */
    public Mono<UserMessageDto> processBirthdayRequest( UserMessageDto userMessageDto,String commandType, int index) {
        SimpleDateFormat mdyFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date currentDate = java.util.Date.from(Instant.now());
        String dateStr = mdyFormat.format(currentDate);

        java.util.Date dob2 = null;
        try {
            dob2 = mdyFormat.parse(dateStr);

            return userRepository.findAllByDob(dob2).collectList().map(new Function<List<User>, UserMessageDto>() {
                @Override
                public UserMessageDto apply(List<User> users) {

                    String message = "";
                    if(userMessageDto.getMessage() != null) {
                        message = userMessageDto.getMessage()+"\n";
                    }

                    if (users.size() == 0){
                        message += "There are no birthdays today.";
                    } else {
                        message += "Please find the list of birthdays for today.\n";
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

    public Mono<UserMessageDto> processNewBirthdayRequest( UserMessage incomingUserMessage, UserMessageDto userMessageDto, UserMessage lastSentMessage) {

        if(lastSentMessage != null && lastSentMessage.getFlow().getCommandType().equals(CommandType.BIRTHDAY)) {
            Flow lastFlow = lastSentMessage.getFlow();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Mono<Pair<Boolean, String>> result = processBirthdayDates(incomingUserMessage);
            return result.map(new Function<Pair<Boolean, String>, Mono<UserMessageDto>>() {
                @Override
                public Mono<UserMessageDto> apply(Pair<Boolean, String> resultPair) {
                    if(resultPair.getFirst().booleanValue() == true) {


                        try {
                            LocalDate dob = LocalDate.parse(incomingUserMessage.getMessage(), formatter);
                            return userRepository.findAllByDob(dob).collectList().map(new Function<List<User>, UserMessageDto>() {
                                @Override
                                public UserMessageDto apply(List<User> users) {

                                    String message = "";
                                    if (userMessageDto.getMessage() != null) {
                                        message = userMessageDto.getMessage() + "\n";
                                    }

                                    if (users.size() == 0) {
                                        message += "There are no birthdays for this date.";
                                    } else {
                                        message += "Please find the list of birthdays below.\n";
                                        for (User u : users) {
                                            message += "" + u.getName();
                                        }
                                    }
                                    message+="\n\nPlease select a option from the list to proceed further.";

                                    userMessageDto.setMessage(message);
                                    userMessageDto.setFlow(null);
                                    userMessageDto.setIndex(0);
                                    MessagePayloadDto payload = MessagePayloadDto.builder()
                                            .message(message)
                                            .msgType(MessagePayloadType.TEXT)
                                            .choices(BotUtil.getCommandChoices())
                                            .build();

                                    userMessageDto.setPayload(payload);

                                    return userMessageDto;
                                }
                            });
                        } catch (Exception e) {
                            return Mono.just(null);
                        }
                    }
                    else {
                        userMessageDto.setMessage(resultPair.getSecond());
                        userMessageDto.setFlow(lastFlow);
                        userMessageDto.setIndex(0);

                        MessagePayloadDto payload = MessagePayloadDto.builder()
                                .message(resultPair.getSecond())
                                .msgType(MessagePayloadType.TEXT)
                                .build();
                        userMessageDto.setPayload(payload);
                        return Mono.just(userMessageDto);
                    }
                }
            }).flatMap(new Function<Mono<UserMessageDto>, Mono<? extends UserMessageDto>>() {
                @Override
                public Mono<? extends UserMessageDto> apply(Mono<UserMessageDto> userMessageDtoMono) {
                    return userMessageDtoMono;
                }
            });
        }
        else{
            return flowRepository.findByIndexAndCommandType(0, CommandType.BIRTHDAY.getDisplayValue()).collectList().map(new Function<List<Flow>, UserMessageDto>() {
                @Override
                public UserMessageDto apply(List<Flow> flow) {
                    if (flow.get(0) != null) {
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
    }

    private Mono<Pair<Boolean, String>> processBirthdayDates( UserMessage incomingUserMessage) {
        Boolean valid = true;
        String errorMsg = "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        try {
            LocalDate date = LocalDate.parse(incomingUserMessage.getMessage(), formatter);
            if(date.compareTo(LocalDate.now()) < 0) {
                valid = false;
                errorMsg = "Entered date should be greator than or equal to current date.";
            }
        } catch (DateTimeParseException e){
            valid = false;
            errorMsg = "Please enter the date in proper format i.e dd-mm-yyyy eg. 11-02-2000";
        }
        return Mono.just(Pair.of(valid, errorMsg));
    }
}
