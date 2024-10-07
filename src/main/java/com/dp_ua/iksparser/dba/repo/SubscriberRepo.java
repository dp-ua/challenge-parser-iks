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

    @Query("SELECT s.participant FROM SubscriberEntity s WHERE s.chatId = :chatId ORDER BY s.participant.surname ASC, s.participant.name ASC")
    Page<ParticipantEntity> findParticipantsByChatId(@Param("chatId") String chatId, Pageable pageable);
}
