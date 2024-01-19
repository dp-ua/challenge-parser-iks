package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.CompetitionEntity;
import com.dp_ua.iksparser.element.DayEntity;
import com.dp_ua.iksparser.element.EventEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepo extends CrudRepository<EventEntity, Long> {
}
