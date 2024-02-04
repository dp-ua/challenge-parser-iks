package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.element.SubscriberEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepo extends CrudRepository<SubscriberEntity, Long> {
    List<SubscriberEntity> findByChatId(String chatId);

    List<SubscriberEntity> findByParticipant(ParticipantEntity participant);

    Optional<SubscriberEntity> findByChatIdAndParticipantId(String chatId, Long id);
}
