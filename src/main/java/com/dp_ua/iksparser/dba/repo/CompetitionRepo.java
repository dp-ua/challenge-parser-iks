package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionRepo extends CrudRepository<CompetitionEntity, Long> {
    CompetitionEntity findByName(String name);

    CompetitionEntity findByNameAndBeginDateAndUrl(String name, String beginDate, String url);

    List<CompetitionEntity> findAllByOrderByUpdated();

    List<CompetitionEntity> findAll();

    void flush();
}

