package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.entity.HeatEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeatRepo extends CrudRepository<HeatEntity, Long> {
}
