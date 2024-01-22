package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.CoachEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoachRepo extends CrudRepository<CoachEntity, Long> {
    CoachEntity findByName(String name);

    List<CoachEntity> findAllByName(String name);

    void flush();
}
