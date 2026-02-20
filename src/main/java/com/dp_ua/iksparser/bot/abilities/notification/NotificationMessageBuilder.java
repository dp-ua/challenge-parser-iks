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
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        var newEnrollmentBlocks = groupByEvents(byStatus.get(false), false);
        var resultBlocks = groupByEvents(byStatus.get(true), true);

        var currentChunk = new MessageChunk(competition);

        currentChunk = addBlocksToChunks(messages, currentChunk, newEnrollmentBlocks, NEW_ATTENDS_TEXT);
        currentChunk = addBlocksToChunks(messages, currentChunk, resultBlocks, RESULTS_TEXT);

        if (!currentChunk.isEmpty()) {
            messages.add(currentChunk.build(null));
        }

        log.debug("Split competition '{}' into {} message(s)",
                competition.getName(), messages.size());

        return messages;
    }

    private MessageChunk addBlocksToChunks(
            List<NotificationMessage> messages,
            MessageChunk currentChunk,
            List<EventBlock> blocks,
            String sectionTitle
    ) {
        if (blocks.isEmpty()) {
            return currentChunk;
        }
        var sectionHeader = buildSectionHeader(sectionTitle);

        for (var eventBlock : blocks) {
            if (!currentChunk.canAdd(sectionHeader + eventBlock.text())) {
                messages.add(currentChunk.build(sectionTitle));
                currentChunk = new MessageChunk(currentChunk.competition);
            }
            if (currentChunk.isEmpty()) {
                currentChunk.addSection(sectionTitle);
            }
            currentChunk.addEventBlock(eventBlock);
        }
        return currentChunk;
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
        var block = new StringBuilder();

        // Заголовок события со ссылкой
        block.append(START)
                .append(" ");

        // Ссылка на событие (стартовый протокол или результаты)
        var eventUrl = hasResults ? event.getResultUrl() : event.getStartListUrl();
        var eventName = cleanMarkdown(event.getEventName());

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

        // Добавляем категорию и раунд
        var eventDetails = new ArrayList<String>();

        if (isNotEmpty(event.getCategory())) {
            eventDetails.add(cleanMarkdown(event.getCategory()));
        }
        if (isNotEmpty(event.getRound())) {
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
        var line = new StringBuilder();

        var participant = notification.getParticipant();
        var heatLine = notification.getHeatLine();
        var heat = heatLine.getHeat();

        // Дополнительная информация: (забег X, доріжка Y, №Z)
        var details = new ArrayList<String>();

        if (isNotEmpty(heat.getName())) {
            var heatName = cleanMarkdown(heat.getName());
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

        line.append("  ")
                .append(ATHLETE)
                .append(" ");

        // Имя участника
        line.append(getShortName(participant));

        if (isNotEmpty(participant.getUrl())) {
            line.append(LINK)
                    .append(" link ")
                    .append(Icon.URL)
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(participant.getUrl())
                    .append(LINK_SEPARATOR_END);
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
        return name + " " + surname;
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

            var header = buildCompetitionHeader(competition);
            this.content.append(header);
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

        boolean canAdd(String text) {
            var currentLength = content.length();
            var additionalLength = text.length();
            return (currentLength + additionalLength) < MAX_CHUNK_SIZE;
        }

        void addSection(String sectionTitle) {
            if (currentSection == null) {
                content.append(END_LINE)
                        .append(sectionTitle)
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
            var finalText = new StringBuilder(content);

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
