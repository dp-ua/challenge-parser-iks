package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.CompetitionEntity;
import com.dp_ua.iksparser.element.DayEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DayRepo extends CrudRepository<DayEntity, Long> {
    DayEntity findByCompetitionAndDayNumber(CompetitionEntity competition, int dayNumber);

    List<DayEntity> findAllByCompetitionOrderByDayNumber(CompetitionEntity competition);}

