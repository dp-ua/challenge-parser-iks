package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.dba.element.DayEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayRepo extends CrudRepository<DayEntity, Long> {
}
