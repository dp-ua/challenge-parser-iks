package com.dp_ua.iksparser.bot.abilities;

import com.dp_ua.iksparser.dba.service.ParticipantService;
import com.dp_ua.iksparser.dba.service.SubscriberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ParticipantsFacadeImpl implements ParticipantFacade {
    @Autowired
    private ParticipantService participantService;
    @Autowired
    SubscriberService subscriberService;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    public void subscribe(String chatId, long commandArgument, Integer editMessageId) {
        log.info("SUBSCRIBE chatId: {}, commandArgument: {}", chatId, commandArgument);
        participantService.findById(commandArgument).ifPresent(participant -> {
            if (subscriberService.isSubscribed(chatId, participant.getId())) {
                log.info("chatId: {} already subscribed to participant: {}", chatId, participant.getId());
            } else {
                subscriberService.subscribe(chatId, participant);
                log.info("chatId: {} subscribed to participant: {}", chatId, participant.getId());
            }
        });
    }

    @Override
    public void unsubscribe(String chatId, long commandArgument, Integer editMessageId) {
        log.info("UNSUBSCRIBE chatId: {}, commandArgument: {}", chatId, commandArgument);
        participantService.findById(commandArgument).ifPresent(participant -> {
            if (subscriberService.isSubscribed(chatId, participant.getId())) {
                subscriberService.unsubscribe(chatId, participant.getId());
                log.info("chatId: {} unsubscribed from participant: {}", chatId, participant.getId());
            } else {
                log.info("chatId: {} not subscribed to participant: {}", chatId, participant.getId());
            }
        });
    }
}
