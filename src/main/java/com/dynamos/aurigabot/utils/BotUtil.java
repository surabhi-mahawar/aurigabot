package com.dynamos.aurigabot.utils;

import java.util.Random;
import java.util.UUID;

public class BotUtil {
    public static String BOT_START_MSG = "Hi Auriga Bot";
    public static String USER_ADMIN = "admin";
    public static UUID USER_ADMIN_ID = UUID.fromString("89326ca8-f4cf-4756-b180-8636824345bd");
    public static String PROVIDER_TRANSPORT_SOCKET = "transport-socket";
    public static String CHANNEL_WEB = "web";
    public static String CHANNEL_TELEGRAM = "telegram";

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
}
