package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.element.SubscriberEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriberRepo extends CrudRepository<SubscriberEntity, Long> {
    List<SubscriberEntity> findByChatId(String chatId);

    List<SubscriberEntity> findByParticipant(ParticipantEntity participant);
}
