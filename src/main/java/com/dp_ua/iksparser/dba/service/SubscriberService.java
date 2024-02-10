package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.element.SubscriberEntity;
import com.dp_ua.iksparser.dba.repo.SubscriberRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SubscriberService {
    private final SubscriberRepo repo;

    @Autowired
    public SubscriberService(SubscriberRepo repo) {
        this.repo = repo;
    }

    public boolean isSubscribed(String chatId, Long id) {
        return repo.findByChatIdAndParticipantId(chatId, id).isPresent();
    }

    public void subscribe(String chatId, ParticipantEntity participant) {
        // check if already subscribed
        boolean isSubscribed = isSubscribed(chatId, participant.getId());
        if (isSubscribed) {
            log.info("chatId: {} already subscribed to participant: {}", chatId, participant.getId());
            return;
        }
        SubscriberEntity subscriber = new SubscriberEntity();
        subscriber.setChatId(chatId);
        subscriber.setParticipant(participant);
        repo.save(subscriber);
    }

    public void unsubscribe(String chatId, Long id) {
        // check if already subscribed
        boolean isSubscribed = isSubscribed(chatId, id);
        if (!isSubscribed) {
            log.info("chatId: {} not subscribed to participant: {}", chatId, id);
            return;
        }
        repo.findByChatIdAndParticipantId(chatId, id).ifPresent(repo::delete);
    }

    public List<SubscriberEntity> findAllByParticipant(ParticipantEntity participant) {
        return repo.findAllByParticipant(participant);
    }

    public List<SubscriberEntity> findAllByChatId(String chatId) {
        return repo.findAllByChatId(chatId);
    }

    public void unsubscribeAll(String chatId) {
        repo.findByChatId(chatId).forEach(repo::delete);
    }
}
