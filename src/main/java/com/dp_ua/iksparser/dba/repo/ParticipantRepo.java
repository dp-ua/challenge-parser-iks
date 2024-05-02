package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRepo extends CrudRepository<ParticipantEntity, Long> {
    List<ParticipantEntity> findAllBySurnameAndNameAndBorn(String surname, String name, String born);

    @Query("SELECT p FROM ParticipantEntity p WHERE lower(p.name) LIKE lower(concat('%', :word, '%')) OR lower(p.surname) LIKE lower(concat('%', :word,'%'))")
    List<ParticipantEntity> findByNameAndSurnameByPart(@Param("word") String word);

    Page<ParticipantEntity> findAll(Pageable pageable);

    @Query("SELECT p FROM ParticipantEntity p WHERE (p.surname, p.name, p.born) IN (SELECT p.surname, p.name, p.born FROM ParticipantEntity p GROUP BY p.surname, p.name, p.born HAVING COUNT(p) > 1)")
    List<ParticipantEntity> findDuplicates();
}
