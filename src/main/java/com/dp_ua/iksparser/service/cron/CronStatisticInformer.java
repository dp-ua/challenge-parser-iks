package com.dp_ua.iksparser.service.cron;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.bot.controller.BotController;
import com.dp_ua.iksparser.dba.entity.StatisticEntity;
import com.dp_ua.iksparser.dba.service.StatisticService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CronStatisticInformer {

    private final StatisticService service;
    private final BotController bot;

    @Scheduled(cron = "0 0 7 * * *")  // every day at 7:00
    public void inform() {
        LocalDate now = LocalDate.now().minusDays(1);
        Map<String, Long> msgCountByChatId = getStatMessagesSorted(now);
        String msg = msgCountByChatId.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
        if (!msg.isEmpty()) {
            bot.sendMessageToAdmin(msg);
        }
    }

    private Map<String, Long> getStatMessagesSorted(LocalDate day) {
        return service.getAllByDate(day).stream()
                .collect(Collectors.groupingBy(StatisticEntity::getChatId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

}