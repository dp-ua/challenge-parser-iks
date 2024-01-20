package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeatLineRepo extends CrudRepository<HeatLineEntity, Long> {
}
