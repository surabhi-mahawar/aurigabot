package com.dynamos.aurigabot.service;

import com.dynamos.aurigabot.dto.MessagePayloadChoiceDto;
import com.dynamos.aurigabot.dto.MessagePayloadDto;
import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.entity.User;
import com.dynamos.aurigabot.entity.UserMessage;
import com.dynamos.aurigabot.enums.CommandType;
import com.dynamos.aurigabot.enums.MessagePayloadType;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import com.dynamos.aurigabot.repository.UserRepository;
import com.dynamos.aurigabot.utils.BotUtil;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Builder
public class MessageService {

    @Autowired
    private UserRepository userRepository;
    /**
     * Process incoming message, return outgoing message
     * @param user
     * @param incomingUserMessage
     * @return
     */
    public Mono<UserMessageDto> processMessage(User user, UserMessage incomingUserMessage) {
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
//        Mono<UserMessageDto> callback;
        if(isBotStartingMessage(incomingUserMessage.getMessage())) {
            return processBotStartingMessage(user, outUserMessageDto);
        } else if(commandType != null) {
            return processCommand(user, outUserMessageDto, commandType);
        }else {
            return processInvalidRequest(outUserMessageDto);
        }

//        return callback;
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
            Map<String, String> data = new HashMap();
            data.put("key", value.toString());
            data.put("value", value.getDisplayValue());
            list.add(data);
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
    public Mono<UserMessageDto> processCommand(User user, UserMessageDto userMessageDto, CommandType commandType) {
        if(commandType.equals(CommandType.BIRTHDAY)) {
            return processMessageRequest( userMessageDto);
        } else {
            return processInvalidRequest(userMessageDto);
        }
    }

    private Mono<UserMessageDto> processMessageRequest( UserMessageDto userMessageDto) {


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
                        message = "There's no birthday today";
                    } else {
                        message = "Please find the list of birthdays for todays.";
                        for (User u : users){
                            message += "\n"+u.getName();
                            System.out.println(u.getDob());
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


    /**
     * Process unidentified/invalid request
     * @param userMessageDto
     * @return
     */
    private Mono<UserMessageDto> processInvalidRequest(UserMessageDto userMessageDto) {
        String msgText = "We do not understand your request, please try: "+BotUtil.BOT_START_MSG;
        userMessageDto.setMessage(msgText);

        MessagePayloadDto payload = MessagePayloadDto.builder()
                .message(msgText)
                .msgType(MessagePayloadType.TEXT)
                .build();
        userMessageDto.setPayload(payload);

        return Mono.just(userMessageDto);
    }
}
