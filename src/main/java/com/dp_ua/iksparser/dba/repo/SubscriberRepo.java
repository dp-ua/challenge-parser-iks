package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.entity.SubscriberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepo extends JpaRepository<SubscriberEntity, Long> {
    List<SubscriberEntity> findByChatId(String chatId);

    List<SubscriberEntity> findByParticipant(ParticipantEntity participant);

    Optional<SubscriberEntity> findByChatIdAndParticipant(String chatId, ParticipantEntity participant);

    void deleteAllByParticipant(ParticipantEntity p);

    @Query(value = "SELECT p.* FROM participant_entity p JOIN subscriber_entity s ON p.id = s.participant_id WHERE s.chat_id = :chatId ORDER BY p.surname COLLATE \"uk-UA-x-icu\" ASC, p.name COLLATE \"uk-UA-x-icu\" ASC", nativeQuery = true)
    Page<ParticipantEntity> findParticipantsByChatId(@Param("chatId") String chatId, Pageable pageable);
}
