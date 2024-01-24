package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.CoachEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoachRepo extends CrudRepository<CoachEntity, Long> {
    List<CoachEntity> findAllByName(String name);

    List<CoachEntity> findByNameContaining(String partialName);
}
