package com.dp_ua.iksparser.dba.repo;

import com.dp_ua.iksparser.dba.element.StatisticEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticRepo extends CrudRepository<StatisticEntity, Long> {
    long countByChatId(String chatId);
}
