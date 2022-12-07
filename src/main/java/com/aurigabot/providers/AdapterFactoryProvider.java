package com.aurigabot.providers;

import com.aurigabot.adapters.AbstractAdapter;
import com.aurigabot.adapters.TelegramAdapter;
import com.aurigabot.adapters.WebPortalAdapter;
import com.aurigabot.enums.MessageChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdapterFactoryProvider {
    @Value("${telegram.bot.apiUrl}")
    private String telegramApiUrl;

    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Value("${web.portal.url}")
    public String webPortalOutboundUrl;

    public AbstractAdapter getAdapter(MessageChannel channel) {
        AbstractAdapter adapter;
        if(channel.equals(MessageChannel.TELEGRAM)) {
            String outboundUrl = telegramApiUrl+"bot"+telegramBotToken;
            adapter = new TelegramAdapter(outboundUrl);
        } else if(channel.equals(MessageChannel.WEB)) {
            adapter = new WebPortalAdapter(webPortalOutboundUrl);
        } else {
            adapter = null;
        }
        return adapter;
    }

}
