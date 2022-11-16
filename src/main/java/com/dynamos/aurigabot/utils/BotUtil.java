package com.dynamos.aurigabot.utils;

import com.dynamos.aurigabot.dto.MessagePayloadChoiceDto;
import com.dynamos.aurigabot.dto.MessagePayloadDto;
import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.enums.CommandType;
import com.dynamos.aurigabot.enums.MessagePayloadType;

import java.util.*;

public class BotUtil {
    public static String BOT_START_MSG = "Hi Auriga Bot";
    public static String USER_ADMIN = "admin";
    public static UUID USER_ADMIN_ID = UUID.fromString("89326ca8-f4cf-4756-b180-8636824345bd");

    /**
     * Check if message equals bot starting message
     * @param message
     * @return
     */
    public static Boolean isBotStartingMessage (String message) {
        return message.trim().equalsIgnoreCase(BotUtil.BOT_START_MSG);
    }

    /**
     * Get user message - message id value
     * @param msgId
     * @return
     */
    public static String getUserMessageId(String msgId) {
        if(msgId != null && !msgId.isEmpty()) {
            return msgId;
        } else {
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Get list of commands for bot
     * @return
     */
    public static List<Map<String, String>> getCommandsList() {
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
     * Get array of messagePayloadChoices
     * @return
     */
    public static ArrayList<MessagePayloadChoiceDto> getCommandChoices() {
        ArrayList<MessagePayloadChoiceDto> choices = new ArrayList<>();
        BotUtil.getCommandsList().forEach(command -> {
            MessagePayloadChoiceDto choice = MessagePayloadChoiceDto.builder()
                    .key(command.get("key"))
                    .text(command.get("value"))
                    .build();
            choices.add(choice);
        });
        return choices;
    }

    /**
     * Check if message is a command
     * @param message
     * @return
     */
    public static CommandType isCommand(String message) {
        message = message.trim();

        for (Map<String, String> command : BotUtil.getCommandsList()) {
            if(message.equalsIgnoreCase(command.get("key"))) {
                return CommandType.valueOf(command.get("key"));
            } else if(message.equalsIgnoreCase((command.get("value")))) {
                return CommandType.getEnumByValue(command.get("value"));
            }
        }
        return null;
    }

    /**
     * Get UserMessageDto for invalid request message
     * @param userMessageDto
     * @return
     */
    public static UserMessageDto getInvalidRequestMessageDto(UserMessageDto userMessageDto) {
        String msgText = "We do not understand your request, please try below commands: ";
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
