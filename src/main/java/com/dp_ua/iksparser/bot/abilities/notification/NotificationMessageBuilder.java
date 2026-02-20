package com.dp_ua.iksparser.bot.abilities.notification;

import static com.dp_ua.iksparser.bot.Icon.ATHLETE;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.bot.abilities.infoview.CompetitionView;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.NotificationQueueEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Builds formatted notification messages grouped by competitions.
 * Handles message chunking for Telegram's 4096 character limit.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationMessageBuilder {

    public static final String NEW_ATTENDS_TEXT = MENU + " НОВІ ЗАЯВКИ:";
    private static final int MAX_CHUNK_SIZE = 4000;
    public static final String RESULTS_TEXT = RESULT + " РЕЗУЛЬТАТИ:";

    private final CompetitionView competitionView;


    /**
     * Build list of messages - one or more per competition (chunked if needed)
     */
    public List<NotificationMessage> buildAggregatedMessages(
            Map<ParticipantEntity, List<NotificationQueueEntity>> grouped) {

        // Группируем по соревнованиям
        Map<CompetitionEntity, List<NotificationQueueEntity>> byCompetition =
                groupByCompetition(grouped);

        // Для каждого соревнования создаем сообщение(я) с учетом chunking
        List<NotificationMessage> allMessages = new ArrayList<>();
        byCompetition.forEach((competition, notifications) -> {
            List<NotificationMessage> competitionMessages =
                    buildMessagesForCompetition(competition, notifications);
            allMessages.addAll(competitionMessages);
        });

        return allMessages;
    }

    /**
     * Группировка по соревнованиям
     */
    private Map<CompetitionEntity, List<NotificationQueueEntity>> groupByCompetition(
            Map<ParticipantEntity, List<NotificationQueueEntity>> byParticipant) {

        return byParticipant.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        notification -> notification.getHeatLine().getHeat()
                                .getEvent().getDay().getCompetition(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * Создает сообщения для одного соревнования с chunking
     */
    private List<NotificationMessage> buildMessagesForCompetition(
            CompetitionEntity competition,
            List<NotificationQueueEntity> notifications) {

        List<NotificationMessage> messages = new ArrayList<>();

        // Разделяем на новые заявки и результаты
        Map<Boolean, List<NotificationQueueEntity>> byStatus = notifications.stream()
                .collect(Collectors.partitioningBy(this::hasResult));

        List<NotificationQueueEntity> newEnrollments = byStatus.get(false);
        List<NotificationQueueEntity> withResults = byStatus.get(true);

        // Группируем по событиям для обработки по частям
        List<EventBlock> newEnrollmentBlocks = groupByEvents(newEnrollments, false);
        List<EventBlock> resultBlocks = groupByEvents(withResults, true);

        // Начинаем формировать chunks
        MessageChunk currentChunk = new MessageChunk(competition);

        // Обрабатываем новые заявки
        if (!newEnrollmentBlocks.isEmpty()) {
            String sectionHeader = buildSectionHeader(NEW_ATTENDS_TEXT);

            for (EventBlock eventBlock : newEnrollmentBlocks) {
                // Проверяем влезет ли этот блок
                if (!currentChunk.canAdd(sectionHeader + eventBlock.text())) {
                    // Сохраняем текущий chunk и начинаем новый
                    messages.add(currentChunk.build(NEW_ATTENDS_TEXT));
                    currentChunk = new MessageChunk(competition);
                }

                if (currentChunk.isEmpty()) {
                    currentChunk.addSection(NEW_ATTENDS_TEXT);
                }

                currentChunk.addEventBlock(eventBlock);
            }
        }

        // Обрабатываем результаты
        if (!resultBlocks.isEmpty()) {
            String sectionHeader = buildSectionHeader(RESULTS_TEXT);

            for (EventBlock eventBlock : resultBlocks) {
                // Проверяем влезет ли этот блок
                if (!currentChunk.canAdd(sectionHeader + eventBlock.text())) {
                    // Сохраняем текущий chunk и начинаем новый
                    messages.add(currentChunk.build(RESULTS_TEXT));
                    currentChunk = new MessageChunk(competition);
                }

                if (currentChunk.isEmpty()) {
                    currentChunk.addSection(RESULTS_TEXT);
                }

                currentChunk.addEventBlock(eventBlock);
            }
        }

        // Добавляем последний chunk если есть контент
        if (!currentChunk.isEmpty()) {
            messages.add(currentChunk.build(null));
        }

        log.debug("Split competition '{}' into {} message(s)",
                competition.getName(), messages.size());

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

        // Группируем по Event (событиям)
        Map<EventEntity, List<NotificationQueueEntity>> byEvent = notifications.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getHeatLine().getHeat().getEvent(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<EventBlock> blocks = new ArrayList<>();
        byEvent.forEach((event, eventNotifications) -> {
            String eventText = buildEventBlock(event, eventNotifications, hasResults);
            List<Long> notificationIds = eventNotifications.stream()
                    .map(NotificationQueueEntity::getId)
                    .toList();
            blocks.add(new EventBlock(eventText, notificationIds));
        });

        return blocks;
    }

    /**
     * Формирует заголовок секции
     */
    private String buildSectionHeader(String title) {
        return END_LINE +
                BOLD + title + BOLD +
                END_LINE + END_LINE;
    }

    /**
     * Формирует блок для одного события
     */
    private String buildEventBlock(EventEntity event,
                                   List<NotificationQueueEntity> notifications,
                                   boolean hasResults) {
        StringBuilder block = new StringBuilder();

        // Заголовок события со ссылкой
        block.append(START)
                .append(" ");

        // Ссылка на событие (стартовый протокол или результаты)
        String eventUrl = hasResults ? event.getResultUrl() : event.getStartListUrl();
        String eventName = cleanMarkdown(event.getEventName());

        if (eventUrl != null && !eventUrl.isEmpty()) {
            block.append(LINK)
                    .append(BOLD)
                    .append(eventName)
                    .append(BOLD)
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(eventUrl)
                    .append(LINK_SEPARATOR_END);
        } else {
            block.append(BOLD)
                    .append(eventName)
                    .append(BOLD);
        }

        // Добавляем категорию и раунд
        List<String> eventDetails = new ArrayList<>();

        if (event.getCategory() != null && !event.getCategory().isEmpty()) {
            eventDetails.add(cleanMarkdown(event.getCategory()));
        }
        if (event.getRound() != null && !event.getRound().isEmpty()) {
            eventDetails.add(cleanMarkdown(event.getRound()));
        }

        if (!eventDetails.isEmpty()) {
            block.append(", ")
                    .append(ITALIC)
                    .append(String.join(" - ", eventDetails))
                    .append(ITALIC);
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
        StringBuilder line = new StringBuilder();

        ParticipantEntity participant = notification.getParticipant();
        HeatLineEntity heatLine = notification.getHeatLine();
        HeatEntity heat = heatLine.getHeat();

        line.append("  ")
                .append(ATHLETE)
                .append(" ");

        // Имя участника со ссылкой
        String participantName = getShortName(participant);

        if (participant.getUrl() != null && !participant.getUrl().isEmpty()) {
            line.append(LINK)
                    .append(participantName)
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(participant.getUrl())
                    .append(LINK_SEPARATOR_END);
        } else {
            line.append(participantName);
        }

        // Дополнительная информация: (заплив X, доріжка Y, №Z)
        List<String> details = new ArrayList<>();

        // Номер забега (если есть название или номер)
        if (heat.getName() != null && !heat.getName().isEmpty()) {
            String heatName = cleanMarkdown(heat.getName());
            details.add("заплив: " + heatName);
        }

        if (heatLine.getLane() != null && !heatLine.getLane().isEmpty()) {
            details.add("доріжка " + heatLine.getLane());
        }

        if (heatLine.getBib() != null && !heatLine.getBib().isEmpty()) {
            details.add("№" + heatLine.getBib());
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

    /**
     * Проверяет есть ли результат (через наличие resultUrl в событии)
     */
    private boolean hasResult(NotificationQueueEntity notification) {
        EventEntity event = notification.getHeatLine().getHeat().getEvent();
        return event.hasResultUrl();
    }

    /**
     * Получить короткое имя участника
     */
    private String getShortName(ParticipantEntity participant) {
        String name = cleanMarkdown(participant.getName());
        String surname = cleanMarkdown(participant.getSurname());
        return name + " " + surname;
    }

    /**
     * Очистка markdown символов
     */
    private String cleanMarkdown(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return SERVICE.cleanMarkdown(text);
    }

    /**
         * Внутренний класс для хранения блока события с текстом и ID уведомлений
         */
        private record EventBlock(String text, List<Long> notificationIds) {

    }

    /**
     * Внутренний класс для накопления частей сообщения (chunk)
     */
    private class MessageChunk {

        private final CompetitionEntity competition;
        private final StringBuilder content;
        private final List<Long> notificationIds;
        private String currentSection;

        MessageChunk(CompetitionEntity competition) {
            this.competition = competition;
            this.content = new StringBuilder();
            this.notificationIds = new ArrayList<>();

            // Формируем заголовок
            String header = buildCompetitionHeader(competition);
            this.content.append(header);
        }

        private String buildCompetitionHeader(CompetitionEntity competition) {
            StringBuilder header = new StringBuilder();
            header.append(SUBSCRIBE)
                    .append(" ")
                    .append(BOLD)
                    .append("Нові оновлення")
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

        boolean canAdd(String text) {
            int currentLength = content.length();
            int additionalLength = text.length();
            return (currentLength + additionalLength) < MAX_CHUNK_SIZE;
        }

        void addSection(String sectionTitle) {
            if (currentSection == null) {
                content.append(END_LINE)
                        .append(BOLD)
                        .append(sectionTitle)
                        .append(BOLD)
                        .append(END_LINE)
                        .append(END_LINE);
                currentSection = sectionTitle;
            }
        }

        void addEventBlock(EventBlock eventBlock) {
            content.append(eventBlock.text());
            notificationIds.addAll(eventBlock.notificationIds());
        }

        boolean isEmpty() {
            return notificationIds.isEmpty();
        }

        NotificationMessage build(String continueSection) {
            StringBuilder finalText = new StringBuilder(content);

            // Если это не последний chunk, добавляем индикатор продолжения
            if (continueSection != null) {
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
