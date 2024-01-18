package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.Competition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionRepo extends CrudRepository<Competition, Long> {
    Competition findByName(String name);

    Competition findByNameAndBeginDateAndUrl(String name, String beginDate, String url);

    List<Competition> findAllByOrderByUpdated();

    List<Competition> findAllByOrderByBeginDate();

    List<Competition> findAllByOrderByBeginDateDesc();
}

