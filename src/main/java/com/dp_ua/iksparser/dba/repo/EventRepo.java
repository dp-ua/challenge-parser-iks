package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.entity.EventEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepo extends CrudRepository<EventEntity, Long> {
    List<EventEntity> findAll();
}
