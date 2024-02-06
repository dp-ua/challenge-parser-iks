package com.dp_ua.iksparser.bot.abilities.subscribe;

import com.dp_ua.iksparser.dba.service.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubscribeFacadeImpl implements SubscribeFacade{
    @Autowired
    SubscriberService subscriberService;
    @Override
    public boolean isSubscribed(String chatId, Long id) {
        return subscriberService.isSubscribed(chatId, id);
    }
}
