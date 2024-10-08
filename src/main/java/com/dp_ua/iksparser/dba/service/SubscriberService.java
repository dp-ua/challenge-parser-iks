package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.entity.SubscriberEntity;
import com.dp_ua.iksparser.dba.repo.SubscriberRepo;
import com.dp_ua.iksparser.service.PageableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SubscriberService {
    private final SubscriberRepo repo;
    @Autowired
    PageableService pageableService;

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

    public int subscribersCount(String chatId){
        return repo.countByChatId(chatId);
    }
    public Page<ParticipantEntity> getSubscriptions(String chatId, int page, int pageSize) {
        Pageable pageRequest = pageableService.createPageRequest(page, pageSize);

        List<ParticipantEntity> participants = repo.findParticipantsByChatId(chatId)
                .stream()
                .sorted(Comparator
                        .comparing(ParticipantEntity::getSurname, Collator.getInstance(new Locale("uk", "UA")))
                        .thenComparing(ParticipantEntity::getName, Collator.getInstance(new Locale("uk", "UA"))))
                .collect(Collectors.toList());

        return pageableService.getPage(participants, pageRequest);
    }

    public void unsubscribeAll(ParticipantEntity p) {
        repo.deleteAllByParticipant(p);
    }
}
