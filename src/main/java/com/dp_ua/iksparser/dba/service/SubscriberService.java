package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.entity.SubscriberEntity;
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

    public boolean isSubscribed(String chatId, ParticipantEntity participant) {
        return repo.findByChatIdAndParticipant(chatId, participant).isPresent();
    }

    public void subscribe(String chatId, ParticipantEntity participant) {
        log.debug("chatId: {} subscribing to participant: {}", chatId, participant.getId());
        if (isSubscribed(chatId, participant)) {
            log.debug("chatId: {} already subscribed to participant: {}", chatId, participant.getId());
            return;
        }
        SubscriberEntity subscriber = new SubscriberEntity();
        subscriber.setChatId(chatId);
        subscriber.setParticipant(participant);
        repo.save(subscriber);
    }

    public void unsubscribe(String chatId, ParticipantEntity participant) {
        log.debug("chatId: {} unsubscribing from participant: {}", chatId, participant.getId());
        repo.findByChatIdAndParticipant(chatId, participant).ifPresent(repo::delete);
    }

    public List<SubscriberEntity> findAllByParticipant(ParticipantEntity participant) {
        return repo.findByParticipant(participant);
    }

    public List<SubscriberEntity> findAllByChatId(String chatId) {
        return repo.findByChatId(chatId);
    }

    public void unsubscribeAll(ParticipantEntity p) {
        repo.deleteAllByParticipant(p);
    }
}
