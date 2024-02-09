package com.dp_ua.iksparser.bot.performer;

import com.dp_ua.iksparser.bot.controller.BotController;
import com.dp_ua.iksparser.bot.event.GetMessageEvent;
import com.dp_ua.iksparser.bot.message.Message;
import com.dp_ua.iksparser.bot.message.SelfMessage;
import com.dp_ua.iksparser.dba.element.SubscriberEntity;
import com.dp_ua.iksparser.dba.service.StatisticService;
import com.dp_ua.iksparser.dba.service.SubscriberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class StatisticPerformer implements ApplicationListener<GetMessageEvent> {
    @Autowired
    StatisticService service;
    @Autowired
    SubscriberService subscriberService;

    @Autowired
    BotController bot;

    @Override
    public void onApplicationEvent(GetMessageEvent event) {
        Message message = event.getMessage();
        String chatId = message.getChatId();
        String name = message.getUserName();
        if (SelfMessage.SELF_USER_NAME.equals(name)) {
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
        List<SubscriberEntity> subscribed = subscriberService.findAllByChatId(chatId);
        subscribed.forEach(subscriber -> {
            log.info("Unsubscribe: " + chatId + " from " + subscriber.getParticipant().getId());
            subscriberService.unsubscribe(chatId, subscriber.getParticipant().getId());
        });
    }

    private void sendMessageToAdmin(String chatId, String name) {
        bot.sendMessageToAdmin("New user:{" + chatId + ", " + name + "}");
    }
}
