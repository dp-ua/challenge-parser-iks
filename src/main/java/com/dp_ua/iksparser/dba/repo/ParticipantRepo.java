package com.dp_ua.iksparser.dba.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

@Repository
public interface ParticipantRepo extends JpaRepository<ParticipantEntity, Long>,
        JpaSpecificationExecutor<ParticipantEntity> {

    List<ParticipantEntity> findAllBySurnameAndNameAndBorn(String surname, String name, String born);

    @Query(value = "SELECT * FROM participant_entity p ORDER BY p.surname COLLATE \"uk-UA-x-icu\" ASC, p.name COLLATE \"uk-UA-x-icu\" ASC",
            nativeQuery = true)
    Page<ParticipantEntity> findAll(Pageable pageable);

    @Query("SELECT p FROM ParticipantEntity p WHERE (p.surname, p.name, p.born) IN (SELECT p.surname, p.name, p.born FROM ParticipantEntity p GROUP" +
            " BY p.surname, p.name, p.born HAVING COUNT(p) > 1)")
    List<ParticipantEntity> findDuplicates();

}
