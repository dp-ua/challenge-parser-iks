package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.HeatLineEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeatLineRepo extends CrudRepository<HeatLineEntity, Long> {
}
