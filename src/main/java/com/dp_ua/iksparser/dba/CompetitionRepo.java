package com.dp_ua.iksparser.dba;

import com.dp_ua.iksparser.element.Competition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetitionRepo extends CrudRepository<Competition, Long> {
    Competition findByName(String name);

    Competition findByNameAndBeginDateAndUrl(String name, String beginDate, String url);
}

