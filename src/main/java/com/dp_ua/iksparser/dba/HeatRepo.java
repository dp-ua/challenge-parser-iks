package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.EventEntity;
import com.dp_ua.iksparser.element.HeatEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeatRepo extends CrudRepository<HeatEntity, Long> {
}