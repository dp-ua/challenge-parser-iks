package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeatLineRepo extends CrudRepository<HeatLineEntity, Long> {
    // use direct SELECT request to get data from DB

    @Query("SELECT hl FROM DayEntity d " +
            "JOIN d.events e " +
            "JOIN e.heats h " +
            "JOIN h.heatLines hl " +
            "JOIN hl.coaches hc " +
            "WHERE d.competition.id = :competitionId AND hc.id = :coachId")
    List<HeatLineEntity> getHeatLinesInCompetitionWhereCoachIs(@Param("competitionId") Long competitionId,
                                                               @Param("coachId") Long coachId);
    // this request is equal to
    // SELECT hl FROM day_entity d
    //JOIN event_entity e ON e.day_id = d.id
    //JOIN heat_entity h ON h.event_id = e.id
    //JOIN heat_line_entity hl ON hl.heat_id = h.id
    //JOIN heatline_coach hc ON hl.id = hc.heatline_id
    //where competition_id = 23 AND coach_id = 125;

    @Query("SELECT hl FROM DayEntity d " +
            "JOIN d.events e " +
            "JOIN e.heats h " +
            "JOIN h.heatLines hl " +
            "WHERE d.competition.id = :competitionId AND hl.bib = :bib")
    List<HeatLineEntity> getHeatLinesInCompetitionByBib(@Param("competitionId") Long competitionId,
                                                        @Param("bib") String bib);
}
