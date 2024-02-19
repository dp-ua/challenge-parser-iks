package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.StatisticEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StatisticRepo extends CrudRepository<StatisticEntity, Long> {
    long countByChatId(String chatId);


    @Query("SELECT e FROM StatisticEntity e WHERE CAST(e.updated AS DATE) = :date")
    List<StatisticEntity> findByUpdatedInDate(@Param("date") LocalDate date);
}
