package com.dynamos.aurigabot.inbound;

import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.dynamos.aurigabot.engine.TelegramService;
import com.dynamos.aurigabot.entity.User;

@Component
public class MessageInbound extends TelegramLongPollingBot{
	private static final Logger log = LoggerFactory.getLogger(MessageInbound.class);

	@Autowired
	private TelegramService telegramService;
	
	@Override
	public void onUpdateReceived(Update update) {
         System.out.println("Inside OnUpdate");
         SendMessage sendMessage = new SendMessage();
        if (update.getMessage().getText().equals("/hello")) {
            log.info("/hello message from user id {} && user name is {}",update.getMessage().getFrom().getId(),update.getMessage().getFrom().getUserName());
            try {
                
                sendMessage.setText("Hello " + update.getMessage().getFrom().getUserName());
                sendMessage.setChatId(update.getMessage().getChatId().toString());
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        
        else if (update .getMessage().getText().equals("/birthday")) {
        	String message = "";
        	for( User s : telegramService.loadAllUserByDOB()) {
        		message += "Name: "+s.getName() + " DOB: "+s.getDob()+"\n";
        	}
        	sendMessage.setText(message);
        	sendMessage.setChatId(update.getMessage().getChatId().toString());
        	
        	try {
        		execute(sendMessage);
        	} catch (TelegramApiException e) {
        		e.printStackTrace();
        	}
        	
        }
		
	}

	@Override
	public String getBotUsername() {
		// TODO Auto-generated method stub
		return "Rahul1676_bot";
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		return "5735779535:AAEenpBPsSnw8_ZmtJn2mt1v5YF_Gk7Nz0I";
	}

}
