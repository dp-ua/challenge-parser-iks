package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.EventEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepo extends CrudRepository<EventEntity, Long> {
}
