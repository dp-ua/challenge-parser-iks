package com.dp_ua.iksparser.dba.service;

import com.dp_ua.iksparser.dba.element.StatisticEntity;
import com.dp_ua.iksparser.dba.repo.StatisticRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StatisticService {
    private final StatisticRepo repo;

    @Autowired
    public StatisticService(StatisticRepo repo) {
        this.repo = repo;
    }

    public void save(String chatId, String text) {
        log.debug("save statistic: chatId={}, text={}", chatId, text);
        repo.save(new StatisticEntity(chatId, text));
    }

    public long getCount(String chatId) {
        return repo.countByChatId(chatId);
    }
}
