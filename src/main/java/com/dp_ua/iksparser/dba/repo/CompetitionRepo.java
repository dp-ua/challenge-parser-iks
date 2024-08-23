package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionRepo extends CrudRepository<CompetitionEntity, Long> {
    CompetitionEntity findByName(String name);

    List<CompetitionEntity> findByNameAndBeginDateAndEndDate(String name, String beginDate, String endDate);

    List<CompetitionEntity> findAllByOrderByUpdated();

    List<CompetitionEntity> findAll();

    void flush();

    @Query("SELECT c FROM CompetitionEntity c " +
            "JOIN c.days d " +
            "JOIN d.events e " +
            "JOIN e.heats h " +
            "JOIN h.heatLines hl " +
            "WHERE hl.participant.id = :id " +
            "GROUP BY c.id " +
            "ORDER BY c.updated DESC")
    Page<CompetitionEntity> findCompetitionsByParticipant(@Param("id") Long participantId, Pageable pageRequest);
}

