package com.dynamos.aurigabot.utils;

import java.util.Random;
import java.util.UUID;

public class BotUtil {
    public static String USER_ADMIN = "admin";
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
