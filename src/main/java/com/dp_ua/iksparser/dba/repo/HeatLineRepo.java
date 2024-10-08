package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeatLineRepo extends CrudRepository<HeatLineEntity, Long> {
    @Query("SELECT hl FROM DayEntity d " +
            "JOIN d.events e " +
            "JOIN e.heats h " +
            "JOIN h.heatLines hl " +
            "JOIN hl.coaches hc " +
            "WHERE d.competition.id = :competitionId AND hc.id = :coachId")
    List<HeatLineEntity> getHeatLinesInCompetitionWhereCoachIs(@Param("competitionId") Long competitionId,
                                                               @Param("coachId") Long coachId);

    @Query("SELECT hl FROM DayEntity d " +
            "JOIN d.events e " +
            "JOIN e.heats h " +
            "JOIN h.heatLines hl " +
            "WHERE d.competition.id = :competitionId AND hl.bib = :bib")
    List<HeatLineEntity> getHeatLinesInCompetitionByBib(@Param("competitionId") Long competitionId,
                                                        @Param("bib") String bib);

    @Query("SELECT hl FROM DayEntity d " +
            "JOIN d.events e " +
            "JOIN e.heats h " +
            "JOIN h.heatLines hl " +
            "WHERE d.competition.id = :competitionId AND lower(hl.participant.surname) LIKE lower(concat('%', :name, '%'))")
    List<HeatLineEntity> getHeatLinesInCompetitionByParticipantSurname(@Param("competitionId") Long competitionId,
                                                                       @Param("name") String name);

    @Query("SELECT hl FROM DayEntity d " +
            "JOIN d.events e " +
            "JOIN e.heats h " +
            "JOIN h.heatLines hl " +
            "JOIN hl.participant p " +
            "WHERE d.competition.id = :competitionId AND p.id = :participantId")
    List<HeatLineEntity> getHeatLinesInCompetitionByParticipantId(@Param("competitionId") Long competitionId,
                                                                  @Param("participantId") Long participantId);
}
