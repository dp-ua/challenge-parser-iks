package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.CoachEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoachRepo extends CrudRepository<CoachEntity, Long> {
}
