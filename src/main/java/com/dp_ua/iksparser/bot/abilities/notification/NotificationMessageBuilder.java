package com.dp_ua.iksparser.bot.abilities.notification;

import static com.dp_ua.iksparser.bot.Icon.MENU;
import static com.dp_ua.iksparser.bot.Icon.RESULT;
import static com.dp_ua.iksparser.bot.Icon.START;
import static com.dp_ua.iksparser.bot.Icon.SUBSCRIBE;
import static com.dp_ua.iksparser.service.MessageCreator.BOLD;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;
import static com.dp_ua.iksparser.service.MessageCreator.ITALIC;
import static com.dp_ua.iksparser.service.MessageCreator.LINK;
import static com.dp_ua.iksparser.service.MessageCreator.LINK_END;
import static com.dp_ua.iksparser.service.MessageCreator.LINK_SEPARATOR;
import static com.dp_ua.iksparser.service.MessageCreator.LINK_SEPARATOR_END;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.infoview.CompetitionView;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.entity.NotificationQueueEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationMessageBuilder {

    public static final String NEW_ATTENDS_TEXT = MENU + " Нові заявки:";
    private static final int MAX_CHUNK_SIZE = 4000;
    public static final String RESULTS_TEXT = RESULT + " Є результати:";

    private final CompetitionView competitionView;

    /**
     * Build list of messages - one or more per competition (chunked if needed)
     */
    public List<NotificationMessage> buildAggregatedMessages(
            Map<CompetitionEntity, List<NotificationQueueEntity>> byCompetition) {

        var allMessages = new ArrayList<NotificationMessage>();
        byCompetition.forEach((competition, notifications) -> {
            var competitionMessages = buildMessagesForCompetition(competition, notifications);
            allMessages.addAll(competitionMessages);
        });

        return allMessages;
    }

    /**
     * Создает сообщения для одного соревнования с chunking
     */
    private List<NotificationMessage> buildMessagesForCompetition(
            CompetitionEntity competition,
            List<NotificationQueueEntity> notifications) {

        var messages = new ArrayList<NotificationMessage>();

        var byStatus = notifications.stream()
                .collect(Collectors.partitioningBy(this::hasResult));

        var newEnrollments = byStatus.get(false);
        var results = byStatus.get(true);

        if (!newEnrollments.isEmpty()) {
            var newEnrollmentBlocks = groupByEvents(newEnrollments, false);
            var enrollmentMessages = buildMessagesForSection(
                    competition,
                    newEnrollmentBlocks,
                    NEW_ATTENDS_TEXT
            );
            messages.addAll(enrollmentMessages);
        }

        if (!results.isEmpty()) {
            var resultBlocks = groupByEvents(results, true);
            var resultMessages = buildMessagesForSection(
                    competition,
                    resultBlocks,
                    RESULTS_TEXT
            );
            messages.addAll(resultMessages);
        }

        log.debug("Split competition '{}' into {} message(s)",
                competition.getName(), messages.size());

        return messages;
    }

    private List<NotificationMessage> buildMessagesForSection(
            CompetitionEntity competition,
            List<EventBlock> blocks,
            String sectionTitle) {

        var messages = new ArrayList<NotificationMessage>();

        if (blocks.isEmpty()) {
            return messages;
        }

        var currentChunk = new MessageChunk(competition, sectionTitle);

        for (var eventBlock : blocks) {
            if (!currentChunk.canAdd(eventBlock.text())) {
                messages.add(currentChunk.build(true)); // true = есть продолжение
                currentChunk = new MessageChunk(competition, sectionTitle);
            }
            currentChunk.addEventBlock(eventBlock);
        }

        if (!currentChunk.isEmpty()) {
            messages.add(currentChunk.build(false)); // false = это последнее сообщение секции
        }

        return messages;
    }

    /**
     * Группирует уведомления по событиям и формирует блоки текста
     */
    private List<EventBlock> groupByEvents(List<NotificationQueueEntity> notifications,
                                           boolean hasResults) {
        if (notifications.isEmpty()) {
            return Collections.emptyList();
        }

        var byEvent = notifications.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getHeatLine().getHeat().getEvent(),
                        Collectors.toList()
                ));

        var blocks = new ArrayList<EventBlock>();
        byEvent.forEach((event, eventNotifications) -> {
            var eventText = buildEventBlock(event, eventNotifications, hasResults);
            var notificationIds = eventNotifications.stream()
                    .map(NotificationQueueEntity::getId)
                    .toList();
            blocks.add(new EventBlock(eventText, notificationIds, event));
        });

        // Сортировка блоков по дню и времени
        blocks.sort((b1, b2) -> {
            var event1 = b1.event();
            var event2 = b2.event();

            // Сначала по дню
            int dayCompare = compareDays(event1.getDay().getDayName(), event2.getDay().getDayName());
            if (dayCompare != 0) {
                return dayCompare;
            }

            // Потом по времени
            return compareTime(event1.getTime(), event2.getTime());
        });

        return blocks;
    }

    /**
     * Сравнение дней (День 1, День 2, ...)
     */
    private int compareDays(String day1, String day2) {
        if (day1 == null && day2 == null) return 0;
        if (day1 == null) return 1;
        if (day2 == null) return -1;

        try {
            // Извлекаем номер дня из строки типа "День 1"
            int num1 = extractDayNumber(day1);
            int num2 = extractDayNumber(day2);
            return Integer.compare(num1, num2);
        } catch (Exception e) {
            // Если не удалось распарсить, сортируем лексикографически
            return day1.compareTo(day2);
        }
    }

    /**
     * Извлекает номер дня из строки "День 1", "День 2" и т.д.
     */
    private int extractDayNumber(String dayName) {
        if (isEmpty(dayName)) {
            return 0;
        }

        // Ищем число в строке
        String[] parts = dayName.split("\\s+");
        for (String part : parts) {
            try {
                return Integer.parseInt(part.trim());
            } catch (NumberFormatException e) {
                // Продолжаем поиск
            }
        }
        return 0;
    }

    /**
     * Сравнение времени (11:00, 12:30, ...)
     */
    private int compareTime(String time1, String time2) {
        if (time1 == null && time2 == null) return 0;
        if (time1 == null) return 1;
        if (time2 == null) return -1;

        try {
            // Преобразуем время в минуты для сравнения
            int minutes1 = timeToMinutes(time1);
            int minutes2 = timeToMinutes(time2);
            return Integer.compare(minutes1, minutes2);
        } catch (Exception e) {
            // Если не удалось распарсить, сортируем лексикографически
            return time1.compareTo(time2);
        }
    }

    /**
     * Преобразует время "11:00" в минуты от начала дня
     */
    private int timeToMinutes(String time) {
        if (isEmpty(time)) {
            return 0;
        }

        String[] parts = time.split(":");
        if (parts.length >= 2) {
            int hours = Integer.parseInt(parts[0].trim());
            int minutes = Integer.parseInt(parts[1].trim());
            return hours * 60 + minutes;
        }
        return 0;
    }

    /**
     * Формирует блок для одного события
     */
    private String buildEventBlock(EventEntity event,
                                   List<NotificationQueueEntity> notifications,
                                   boolean hasResults) {
        var block = new StringBuilder();

        // Заголовок события со ссылкой
        block.append(START)
                .append(" ")
                .append(event.getDay().getDayName())
                .append(", ")
                .append(event.getTime())
                .append(END_LINE);

        // Ссылка на событие (стартовый протокол или результаты)
        var eventUrl = hasResults ? event.getResultUrl() : event.getStartListUrl();

        // Добавляем категорию и раунд
        var eventDetails = new ArrayList<String>();

        if (isNotEmpty(event.getCategory())) {
            eventDetails.add(cleanMarkdown(event.getCategory()));
        }
        if (isNotEmpty(event.getRound())) {
            eventDetails.add(cleanMarkdown(event.getRound()));
        }

        var eventName = new StringBuilder(cleanMarkdown(event.getEventName()));

        if (!eventDetails.isEmpty()) {
            eventName.append(", ")
                    .append(String.join(" - ", eventDetails));
        }

        if (isNotEmpty(eventUrl)) {
            block
                    .append(LINK)
                    .append(eventName)
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(eventUrl)
                    .append(LINK_SEPARATOR_END);
        } else {
            block.append(BOLD)
                    .append(eventName)
                    .append(BOLD);
        }

        block.append(END_LINE);

        // Список участников
        notifications.forEach(notification -> block.append(buildParticipantLine(notification)));

        block.append(END_LINE);
        return block.toString();
    }

    /**
     * Формирует строку для одного участника
     */
    private String buildParticipantLine(NotificationQueueEntity notification) {
        var line = new StringBuilder();

        var participant = notification.getParticipant();
        var heatLine = notification.getHeatLine();
        var heat = heatLine.getHeat();

        // иконка
        var heatNumber = heat.extractHeatNumber();
        if (ObjectUtils.isNotEmpty(heatNumber)) {
            line
                    .append(Icon.getIconicNumber(heatNumber))
                    .append(" ");
        }

        // Имя участника
        var participantName = getShortName(participant);

        if (isNotEmpty(participant.getUrl())) {
            line.append(LINK)
                    .append(participantName)
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(participant.getUrl())
                    .append(LINK_SEPARATOR_END);
        } else {
            line.append(participantName);
        }

        // Дополнительная информация: (забег X, доріжка Y, №Z)
        var details = new ArrayList<String>();

        if (isNotEmpty(heat.getName())) {
            var heatName = ObjectUtils.isNotEmpty(heatNumber)
                    ? "з." + heatNumber
                    : cleanMarkdown(heat.getName());
            details.add(heatName);
        }

        if (isNotEmpty(heatLine.getLane())) {
            details.add("д." + heatLine.getLane());
        }

        if (isNotEmpty(heatLine.getBib())) {
            details.add("bib." + heatLine.getBib());
        }

        if (!details.isEmpty()) {
            line.append(" ")
                    .append(ITALIC)
                    .append("(")
                    .append(String.join(", ", details))
                    .append(")")
                    .append(ITALIC);
        }

        line.append(END_LINE);
        return line.toString();
    }

    private boolean hasResult(NotificationQueueEntity notification) {
        var event = notification.getHeatLine().getHeat().getEvent();
        return event.hasResultUrl();
    }

    private String getShortName(ParticipantEntity participant) {
        var name = cleanMarkdown(participant.getName());
        var surname = cleanMarkdown(participant.getSurname());
        return surname + " " + name;
    }

    /**
     * Очистка markdown символов
     */
    private String cleanMarkdown(String text) {
        if (isEmpty(text)) {
            return "";
        }
        return SERVICE.cleanMarkdown(text);
    }

    /**
     * Внутренний класс для хранения блока события с текстом и ID уведомлений
     */
    private record EventBlock(String text, List<Long> notificationIds, EventEntity event) {

    }

    /**
     * Внутренний класс для накопления частей сообщения (chunk)
     */
    private class MessageChunk {

        private final CompetitionEntity competition;
        private final StringBuilder content;
        private final List<Long> notificationIds;

        MessageChunk(CompetitionEntity competition, String sectionTitle) {
            this.competition = competition;
            this.content = new StringBuilder();
            this.notificationIds = new ArrayList<>();

            var header = buildCompetitionHeader(competition);
            this.content.append(header);

            this.content.append(buildSectionHeader(sectionTitle));
        }

        private String buildCompetitionHeader(CompetitionEntity competition) {
            var header = new StringBuilder();
            header.append(SUBSCRIBE)
                    .append(" ")
                    .append(BOLD)
                    .append("Підписка")
                    .append(BOLD)
                    .append(END_LINE)
                    .append(END_LINE)
                    .append(START)
                    .append(" ")
                    .append(competitionView.nameAndDate(competition))
                    .append(END_LINE)
                    .append(competitionView.area(competition))
                    .append(END_LINE);
            return header.toString();
        }

        private String buildSectionHeader(String title) {
            return END_LINE +
                    BOLD + title + BOLD +
                    END_LINE + END_LINE;
        }

        boolean canAdd(String text) {
            var currentLength = content.length();
            var additionalLength = text.length();
            return (currentLength + additionalLength) < MAX_CHUNK_SIZE;
        }

        void addEventBlock(EventBlock eventBlock) {
            content.append(eventBlock.text());
            notificationIds.addAll(eventBlock.notificationIds());
        }

        boolean isEmpty() {
            return notificationIds.isEmpty();
        }

        NotificationMessage build(boolean hasContinuation) {
            var finalText = new StringBuilder(content);

            if (hasContinuation) {
                finalText.append(END_LINE)
                        .append(ITALIC)
                        .append("... продовження в наступному повідомленні")
                        .append(ITALIC);
            }

            return NotificationMessage.builder()
                    .text(finalText.toString())
                    .competition(competition)
                    .notificationIds(new ArrayList<>(notificationIds))
                    .build();
        }

    }

}
