package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepo extends CrudRepository<ParticipantEntity, Long> {
    List<ParticipantEntity> findAllBySurnameAndNameAndTeamAndRegionAndBorn(String surname, String name, String team, String region, String born);

    @Query("SELECT p FROM ParticipantEntity p WHERE lower(p.name) LIKE lower(concat('%', :word, '%')) OR lower(p.surname) LIKE lower(concat('%', :word,'%'))")
    List<ParticipantEntity> findByNameAndSurnameByPart(@Param("word") String word);
}
