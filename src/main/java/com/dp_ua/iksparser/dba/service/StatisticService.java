package com.dp_ua.iksparser.dba.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.dba.entity.StatisticEntity;
import com.dp_ua.iksparser.dba.repo.StatisticRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class StatisticService {

    private final StatisticRepo repo;


    public void save(String chatId, String name, String text) {
        log.debug("save statistic: chatId={}, text={}", chatId, text);
        repo.save(new StatisticEntity(chatId, name, text));
    }

    public long getCount(String chatId) {
        return repo.countByChatId(chatId);
    }

    public List<StatisticEntity> getAllByDate(LocalDate date) {
        return repo.findByUpdatedInDate(date);
    }

}
