package com.dp_ua.iksparser.bot.performer;

import com.dp_ua.iksparser.bot.controller.BotController;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.bot.performer.event.GetMessageEvent;
import com.dp_ua.iksparser.dba.service.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StatisticPerformer implements ApplicationListener<GetMessageEvent> {
    @Autowired
    StatisticService service;
    @Autowired
    BotController bot;

    @Override
    public void onApplicationEvent(GetMessageEvent event) {
        Message message = event.getMessage();
        String chatId = message.getChatId();
        // todo user name
//        message.getUserName();
        long count = service.getCount(chatId);
        if (count == 0)
            sendMessageToAdmin(chatId);
        String text = message.getMessageText();
        service.save(chatId, text);
    }

    private void sendMessageToAdmin(String chatId) {
        bot.sendMessageToAdmin("New user:{" + chatId + "}");
    }
}
