package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantRepo extends CrudRepository<ParticipantEntity, Long> {
    ParticipantEntity findByUrl(String url);

    ParticipantEntity findBySurnameAndNameAndTeamAndRegionAndBorn(String surname, String name, String team, String region, String born);
}
