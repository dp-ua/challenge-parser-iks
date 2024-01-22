package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepo extends CrudRepository<ParticipantEntity, Long> {
    ParticipantEntity findBySurnameAndNameAndTeamAndRegionAndBorn(String surname, String name, String team, String region, String born);

    List<ParticipantEntity> findAllBySurnameAndNameAndTeamAndRegionAndBorn(String surname, String name, String team, String region, String born);
}
