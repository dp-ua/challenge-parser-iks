package com.dp_ua.iksparser.bot.performer;

import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.controller.BotController;
import com.dp_ua.iksparser.bot.event.GetMessageEvent;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.bot.message.SelfMessage;
import com.dp_ua.iksparser.dba.service.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import static com.dp_ua.iksparser.bot.message.SelfMessage.SELF_USER_NAME;

@Component
@Slf4j
public class StatisticPerformer implements ApplicationListener<GetMessageEvent> {
    @Autowired
    StatisticService service;
    @Autowired
    SubscribeFacade subscribeFacade;

    @Autowired
    BotController bot;

    @Override
    public void onApplicationEvent(GetMessageEvent event) {
        Message message = event.getMessage();
        String chatId = message.getChatId();
        String name = message.getUserName();
        if (SELF_USER_NAME.equals(name)) {
            return;
        }
        if (message.kickBot()) {
            log.info("User kick bot: " + chatId);
            kickAction(chatId, name);
            return;
        }
        long count = service.getCount(chatId);
        if (count == 0) {
            sendMessageToAdmin(chatId, name);
        }
        String text = message.getMessageText();
        service.save(chatId, name, text);
    }

    private void kickAction(String chatId, String name) {
        bot.sendMessageToAdmin("User kick bot: " + chatId + ", {" + name + "}");
        subscribeFacade.unsubscribe(chatId);
    }

    private void sendMessageToAdmin(String chatId, String name) {
        bot.sendMessageToAdmin("New user:{" + chatId + ", " + name + "}");
    }
}
