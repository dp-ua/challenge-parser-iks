package com.dp_ua.iksparser.bot.abilities;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.command.impl.CommandSearchByCoach;
import com.dp_ua.iksparser.bot.command.impl.CommandSearchByCoachWithName;
import com.dp_ua.iksparser.bot.command.impl.CommandSearchByName;
import com.dp_ua.iksparser.bot.command.impl.CommandSearchByNameWithName;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.bot.event.UpdateCompetitionEvent;
import com.dp_ua.iksparser.dba.element.*;
import com.dp_ua.iksparser.dba.service.CoachService;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.service.JsonReader;
import com.dp_ua.iksparser.service.parser.MainParserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.bot.event.SendMessageEvent.MsgType.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
@Slf4j
public class CompetitionFacadeImpl implements CompetitionFacade {
    public static final int COMPETITIONS_PAGE_SIZE = 3;
    private static final int COMPETITION_BUTTON_LIMIT = 40;
    public static final int TTL_MINUTES_COMPETITION_UPDATE = 30;
    public static final int MAX_PARTICIPANTS_SIZE_TO_FIND = 5;
    private static final int MAX_CHUNK_SIZE = 4096;

    @Autowired
    MainParserService mainPageParser;
    @Autowired
    CompetitionService competitionService;
    @Autowired
    CoachService coachService;
    @Autowired
    ApplicationEventPublisher publisher;
    @Autowired
    StateService stateService;
    @Autowired
    JsonReader jSonReader;
    @Autowired
    StateService state;


    @Override
    public void showCompetitions(String chatId, int pageNumber, Integer editMessageId) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number must be positive");
        }
        log.info("showCompetitions. Page {}, chatId:{} ", pageNumber, chatId);
        sendTypingAction(chatId);

        List<CompetitionEntity> competitions = getCompetitions();

        publishEvent(getMessageForCompetitions(competitions, chatId, editMessageId, pageNumber));
    }

    @Override
    @Transactional
    public void showCompetition(String chatId, int commandArgument, Integer editMessageId) {
        log.info("showCompetition. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        CompetitionEntity competition = competitionService.findById(commandArgument);

        if (competition == null) {
            log.warn("Competition[{}] not found", commandArgument);
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Змагання не знайдено", getBackToCompetitionsKeyboard()));
            return;
        }

        boolean competitionFilled = isCompetitionFilled(competition);
        String text = getCompetitionDetailsText(competition, competitionFilled);
        if (!competitionFilled) {
            text = text + updatingMessageText();
        }
        InlineKeyboardMarkup keyboard = getCompetitionDetailsKeyboard(competition, competitionFilled);

        publishEvent(prepareSendMessageEvent(
                chatId,
                editMessageId,
                text,
                keyboard
        ));
        if (!competitionFilled) {
            log.info("Competition[{}] is not filled. Going to update", competition.getId());
            publishUpdateCompetitionMessage(chatId, editMessageId, competition);
        }
    }

    @Override
    public void startSearchByName(String chatId, int commandArgument, Integer editMessageId) {
        log.info("startSearchByName. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        CompetitionEntity competition = competitionService.findById(commandArgument);
        if (competition == null) {
            log.warn("Competition[{}] not found", commandArgument);
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Змагання не знайдено", getBackToCompetitionsKeyboard()));
            return;
        }
        String competitionId = competition.getId().toString();
        stateService.setState(chatId, CommandSearchByNameWithName.getTextForState(competitionId));
        StringBuilder sb = getFindByNameMessage(competition);
        publishEvent(prepareSendMessageEvent(
                chatId,
                editMessageId,
                sb.toString(),
                getBackToCompetitionKeyboard(competitionId)
        ));
    }

    @Override
    @Transactional
    public void searchingByName(String chatId, String commandArgument, Integer editMessageId) {
        log.info("searchingByName. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        String competitionId = jSonReader.getVal(commandArgument, "id");
        String name = jSonReader.getVal(commandArgument, "name");

        CompetitionEntity competition = competitionService.findById(Integer.parseInt(competitionId));

        if (!isValidInputNameConditions(chatId, editMessageId, competition, competitionId, name)) return;

        List<HeatLineEntity> heatLines = competition.getDays().stream()
                .flatMap(day -> day.getEvents().stream())
                .flatMap(event -> event.getHeats().stream())
                .flatMap(heap -> heap.getHeatLines().stream())
                .filter(heatLine -> heatLine.getParticipant()
                        .getSurname().toLowerCase()
                        .contains(name.toLowerCase())
                )
                .toList();
        if (heatLines.isEmpty()) {
            log.warn("No participants found");
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Учасників не знайдено", getBackToCompetitionKeyboard(competitionId)));
            return;
        }
        Set<ParticipantEntity> participants = heatLines.stream()
                .map(HeatLineEntity::getParticipant)
                .collect(Collectors.toSet());
        if (participants.size() > MAX_PARTICIPANTS_SIZE_TO_FIND) {
            log.warn("More than one participant found");
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Знайдено забагато спортсменів. Введіть повніше прізвище", getBackToCompetitionKeyboard(competitionId)));
            return;
        }
        participants.forEach(participant -> {
            StringBuilder sb = new StringBuilder();
            sb
                    .append(LOOK)
                    .append(BOLD)
                    .append("Знайдено спортсмена: ")
                    .append(BOLD)
                    .append(END_LINE);
            sb
                    .append(participantInfo(participant))
                    .append(END_LINE)
                    .append(END_LINE);

            sb
                    .append(competitionName(competition))
                    .append(END_LINE)
                    .append(competitionDate(competition))
                    .append(END_LINE)
                    .append(END_LINE);
            sb
                    .append(HEAT)
                    .append("Приймає участь у змаганнях: ")
                    .append(END_LINE);

            heatLines.forEach(heatLine -> sb.append(heatLineInfo(heatLine)));

            publishEvent(prepareSendMessageEvent(
                    chatId,
                    null,
                    sb.toString(),
                    getBackToCompetitionKeyboard(competitionId)
            ));
        });

    }

    @Override
    public void startSearchByCoach(String chatId, int commandArgument, Integer editMessageId) {
        log.info("startSearchByCoach. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        CompetitionEntity competition = competitionService.findById(commandArgument);
        if (competition == null) {
            log.warn("Competition[{}] not found", commandArgument);
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Змагання не знайдено", getBackToCompetitionsKeyboard()));
            return;
        }
        String competitionId = competition.getId().toString();
        stateService.setState(chatId, CommandSearchByCoachWithName.getTextForState(competitionId));
        StringBuilder sb = getFindByCoachMessage(competition);
        publishEvent(prepareSendMessageEvent(
                chatId,
                editMessageId,
                sb.toString(),
                getBackToCompetitionKeyboard(competitionId)
        ));
    }

    private StringBuilder getFindByCoachMessage(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(LOOK)
                .append("Напишіть в чат прізвище тренера(або частину), якого шукаєте")
                .append(END_LINE)
                .append(END_LINE)
                .append(FIND)
                .append(" Пошук спортсменів по тренеру буде проводитись в івенті: ")
                .append(END_LINE)
                .append(competitionName(competition))
                .append(END_LINE)
                .append(competitionDate(competition))
                .append(END_LINE)
                .append(competitionArea(competition));
        return sb;
    }

    @Override
    @Transactional
    public void searchingByCoachWithName(String chatId, String commandArgument, Integer editMessageId) {
        log.info("searchingByCoachWithName. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        String competitionId = jSonReader.getVal(commandArgument, "id");
        String coachName = jSonReader.getVal(commandArgument, "coachName");
        CompetitionEntity competition = competitionService.findById(Integer.parseInt(competitionId));

        if (!isValidInputNameConditions(chatId, editMessageId, competition, competitionId, coachName)) return;

        List<CoachEntity> foundCoaches = coachService.searchByNamePartialMatch(coachName);
        if (checkFoundCoaches(chatId, editMessageId, foundCoaches, coachName)) return;

        List<HeatLineEntity> heatLines = competition.getDays().stream()
                .flatMap(day -> day.getEvents().stream())
                .flatMap(event -> event.getHeats().stream())
                .flatMap(heap -> heap.getHeatLines().stream())
                .toList();
        Map<CoachEntity, List<HeatLineEntity>> coachHeatLinesMap = new HashMap<>();
        foundCoaches.forEach(coach -> {
            List<HeatLineEntity> coachHeatLines = heatLines.stream()
                    .filter(heatLine -> heatLine.getCoaches().contains(coach))
                    .collect(Collectors.toList());
            coachHeatLinesMap.put(coach, coachHeatLines);
        });
        if (coachHeatLinesMap.isEmpty()) {
            log.warn("No participants found");
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Учасників не знайдено", getBackToCompetitionKeyboard(competitionId)));
            return;
        }
        coachHeatLinesMap.forEach((coach, coachHeatLines) -> {
            Map<ParticipantEntity, List<HeatLineEntity>> participantHeatLinesMap = new HashMap<>();
            coachHeatLines.forEach(heatLine -> {
                ParticipantEntity participant = heatLine.getParticipant();
                List<HeatLineEntity> participantHeatLines =
                        participantHeatLinesMap.computeIfAbsent(participant, k -> new ArrayList<>());
                participantHeatLines.add(heatLine);
            });

            StringBuilder header = new StringBuilder();
            header
                    .append(LOOK)
                    .append(BOLD)
                    .append("Знайдено тренера: ")
                    .append(BOLD)
                    .append(END_LINE)
                    .append(COACH)
                    .append(BOLD)
                    .append(coach.getName())
                    .append(BOLD)
                    .append(END_LINE)
                    .append(END_LINE);

            header
                    .append(competitionName(competition))
                    .append(END_LINE)
                    .append(competitionDate(competition))
                    .append(END_LINE)
                    .append(END_LINE);
            header
                    .append(ITALIC)
                    .append("Заявлені такі спортсмени: ")
                    .append(ITALIC)
                    .append(END_LINE)
                    .append(END_LINE);

            List<StringBuilder> participantsInfo = prepareParticipantsInfoList(participantHeatLinesMap);
            sendChunkedMessages(chatId, competitionId, header, participantsInfo);
        });
    }

    private void sendChunkedMessages(String chatId, String competitionId, StringBuilder header, List<StringBuilder> participantsInfo) {
        StringBuilder message = new StringBuilder();
        message.append(header);
        participantsInfo.forEach(participantInfo -> {
            if (message.toString().length() + participantInfo.toString().length() >= MAX_CHUNK_SIZE) {
                publishEvent(prepareSendMessageEvent(
                        chatId,
                        null,
                        message.toString(),
                        getBackToCompetitionKeyboard(competitionId)
                ));
                message.setLength(0);
                message.append(header);
            }
            message.append(participantInfo);
        });

        publishEvent(prepareSendMessageEvent(
                chatId,
                null,
                message.toString(),
                getBackToCompetitionKeyboard(competitionId)
        ));
    }

    private List<StringBuilder> prepareParticipantsInfoList(Map<ParticipantEntity, List<HeatLineEntity>> participantHeatLinesMap) {
        List<StringBuilder> result = new ArrayList<>();
        participantHeatLinesMap.forEach((participant, participantHeatLines) -> {
            StringBuilder participantInfo = new StringBuilder();
            participantInfo
                    .append(participantInfo(participant))
                    .append(END_LINE);
            participantHeatLines.forEach(heatLine -> participantInfo.append(heatLineInfo(heatLine)));
            participantInfo.append(END_LINE);
            result.add(participantInfo);
        });
        return result;
    }

    @Override
    public void showNotLoadedInfo(String chatId, int commandArgument, Integer editMessageId) {
        log.info("showNotLoadedInfo. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        CompetitionEntity competition = competitionService.findById(commandArgument);

        if (competition == null) {
            log.warn("Competition[{}] not found", commandArgument);
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Змагання не знайдено", getBackToCompetitionsKeyboard()));
            return;
        }

        String text = getCompetitionDetailsText(competition, false);
        InlineKeyboardMarkup keyboard = getCompetitionDetailsKeyboard(competition, false);

        publishEvent(prepareSendMessageEvent(
                chatId,
                editMessageId,
                text,
                keyboard
        ));
    }

    private String heatLineInfo(HeatLineEntity heatLine) {
        StringBuilder sb = new StringBuilder();
        HeatEntity heat = heatLine.getHeat();
        EventEntity event = heat.getEvent();
        DayEntity day = event.getDay();

        sb
                .append(MARK)
                .append(" ")
                .append(day.getDayName())
                .append(", ")
                .append(event.getTime())
                .append(", ");
        if (!event.getStartListUrl().isEmpty()) {
            sb
                    .append(LINK)
                    .append(event.getEventName())
                    .append(", ")
                    .append(event.getCategory())
                    .append(", ")
                    .append(event.getRound())
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(event.getStartListUrl())
                    .append(LINK_SEPARATOR_END);
        } else {
            sb.append(event.getEventName())
                    .append(", ")
                    .append(event.getRound());
        }
        sb

                .append(", ")
                .append(heat.getName())
                .append(", д.")
                .append(heatLine.getLane())
                .append(",bib.")
                .append(heatLine.getBib())
                .append(END_LINE);
        return sb.toString();
    }

    private String participantInfo(ParticipantEntity participant) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(ATHLETE)
                .append(participant.getSurname())
                .append(" ")
                .append(participant.getName());
        if (!participant.getUrl().isEmpty()) {
            sb
                    .insert(0, LINK);
            sb
                    .append(" ")
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(participant.getUrl())
                    .append(LINK_SEPARATOR_END);
        }
        sb
                .append(" ")
                .append(participant.getBorn())
                .append(BIRTHDAY);
        sb
                .append(END_LINE)
                .append(AREA)
                .append(participant.getRegion())
                .append(", ")
                .append(participant.getTeam());
        return sb.toString();
    }

    private boolean checkFoundCoaches(String chatId, Integer editMessageId, List<CoachEntity> foundCoaches, String coachName) {
        if (foundCoaches.isEmpty()) {
            log.warn("No coaches found: {}", coachName);
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Тренера не знайдено.", null));
            return true;
        }
        return false;
    }

    private boolean isValidInputNameConditions(String chatId, Integer editMessageId, CompetitionEntity competition, String id, String name) {
        if (competition == null) {
            log.warn("Competition[{}] not found", id);
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Змагання не знайдено", getBackToCompetitionsKeyboard()));
            return false;
        }
        if (name.isEmpty()) {
            log.warn("Name is empty");
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Ви не вказали прізвище", getBackToCompetitionsKeyboard()));
            return false;
        }

        if (name.length() < 3) {
            log.warn("Name is too short");
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Ви вказали занадто коротке прізвище", getBackToCompetitionsKeyboard()));
            return false;
        }
        if (!name.matches("[а-яА-Яa-zA-Z-ґҐєЄіІїЇ']+")) {
            log.warn("Name contains invalid symbols: " + name);
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Ви вказали некоректне прізвище", getBackToCompetitionsKeyboard()));
            return false;
        }
        return true;
    }

    private StringBuilder getFindByNameMessage(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("Напишіть в чат прізвище спортсмена(або частину), якого шукаєте")
                .append(LOOK)
                .append(END_LINE)
                .append(END_LINE)
                .append(FIND)
                .append("Пошук участі спортсмена буде проводитись в івенті: ")
                .append(END_LINE)
                .append(competitionName(competition))
                .append(END_LINE)
                .append(competitionDate(competition))
                .append(END_LINE)
                .append(competitionArea(competition));
        return sb;
    }

    private void publishUpdateCompetitionMessage(String chatId, Integer editMessageId, CompetitionEntity competition) {
        // todo move to separate class
        UpdateStatusEntity message = new UpdateStatusEntity();
        message.setChatId(chatId);
        message.setCompetitionId(competition.getId());
        message.setEditMessageId(editMessageId);
        UpdateCompetitionEvent updateCompetitionEvent = new UpdateCompetitionEvent(this, message);
        publisher.publishEvent(updateCompetitionEvent);
    }

    private InlineKeyboardMarkup getBackToCompetitionsKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = getBackButton("/competitions");
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private InlineKeyboardMarkup getBackToCompetitionKeyboard(String competitionId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = getBackButton("/competition " + competitionId);
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void publishEvent(SendMessageEvent chatId) {
        publisher.publishEvent(chatId);
    }

    private boolean isCompetitionFilled(CompetitionEntity competition) {
        return !competition.getDays().isEmpty();
    }

    private SendMessageEvent prepareSendMessageEvent(String chatId, Integer editMessageId, String text, InlineKeyboardMarkup keyboard) {
        SendMessageEvent sendMessageEvent;
        String prepareText = SERVICE.maskApostrof(text);
        if (editMessageId == null) {
            SendMessage message = SERVICE.getSendMessage(
                    chatId,
                    prepareText,
                    keyboard,
                    true
            );
            message.disableWebPagePreview();
            sendMessageEvent = new SendMessageEvent(this, message, SEND_MESSAGE);
        } else {
            EditMessageText editMessageText = SERVICE.getEditMessageText(
                    chatId,
                    editMessageId,
                    prepareText,
                    keyboard,
                    true);
            editMessageText.disableWebPagePreview();
            sendMessageEvent = new SendMessageEvent(this, editMessageText, EDIT_MESSAGE);
        }
        return sendMessageEvent;
    }

    private InlineKeyboardMarkup getCompetitionDetailsKeyboard(CompetitionEntity competition, boolean competitionFilled) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(getBackButton("/competitions"));
        if (competitionFilled) {
            rows.add(lookAtCompetitionByNameButton(competition));
            rows.add(lookAtCompetitionByCoachButton(competition));
        }
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private List<InlineKeyboardButton> lookAtCompetitionByNameButton(CompetitionEntity competition) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                FIND + " Пошук за прізвищем спортсмена " + ATHLETE,
                "/" + CommandSearchByName.command + " " + competition.getId()
        );
        row.add(button);
        return row;
    }

    private List<InlineKeyboardButton> lookAtCompetitionByCoachButton(CompetitionEntity competition) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                FIND + " Пошук за прізвищем тренера " + COACH,
                "/" + CommandSearchByCoach.command + " " + competition.getId()
        );
        row.add(button);
        return row;
    }

    private static List<InlineKeyboardButton> getBackButton(String callbackData) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                BACK + " Назад", callbackData
        );
        row.add(button);
        return row;
    }

    private String getCompetitionDetailsText(CompetitionEntity competition, boolean competitionFilled) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(competitionName(competition))
                .append(END_LINE)
                .append(END_LINE);
        sb
                .append(competitionDate(competition))
                .append(END_LINE);
        sb
                .append(competitionArea(competition))
                .append(competitionLink(competition))
                .append(END_LINE)
                .append(END_LINE);

        if (competitionFilled) {
            sb
                    .append(competitionDetails(competition))
                    .append(END_LINE);
        } else {
            sb.append(notFilledInfo());
        }
        return sb.toString();
    }

    private StringBuilder notFilledInfo() {
        StringBuilder sb = new StringBuilder();
        sb
                .append(WARNING)
                .append(" Детальна інформація про змагання відсутня ")
                .append(WARNING)
                .append(END_LINE);
        return sb;
    }

    private StringBuilder updatingMessageText() {
        StringBuilder sb = new StringBuilder();
        sb
                .append(END_LINE)
                .append(INFO)
                .append(" Оновлюю. Вам прийде повідомлення, якщо дані вже доступні ")
                .append(END_LINE);
        return sb;
    }

    private StringBuilder competitionDetails(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(EVENT)
                .append(" Тривалість днів: ")
                .append(BOLD)
                .append(competition.getDays().size())
                .append(BOLD)
                .append(END_LINE);

        int participantsCount = competition.getDays().stream()
                .flatMap(day -> day.getEvents().stream())
                .flatMap(event -> event.getHeats().stream())
                .flatMap(heap -> heap.getHeatLines().stream())
                .map(HeatLineEntity::getParticipant)
                .collect(Collectors.toSet()).size();
        sb
                .append(TEAM)
                .append(" Кількість учасників: ")
                .append(BOLD)
                .append(participantsCount)
                .append(BOLD)
                .append(END_LINE);

        int heatCount = competition.getDays().stream()
                .flatMap(day -> day.getEvents().stream())
                .flatMap(event -> event.getHeats().stream())
                .collect(Collectors.toSet()).size();
        sb
                .append(START)
                .append(" Кількість стартів: ")
                .append(BOLD)
                .append(heatCount)
                .append(BOLD)
                .append(END_LINE);
        return sb;
    }

    private StringBuilder competitionLink(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        if (competition.getUrl().isEmpty()) return sb;
        sb
                .append(LINK)
                .append(", посилання")
                .append(Icon.URL)
                .append(LINK_END)
                .append(LINK_SEPARATOR)
                .append(competition.getUrl())
                .append(LINK_SEPARATOR_END);
        return sb;
    }

    private StringBuilder competitionArea(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(AREA)
                .append(ITALIC)
                .append(" Місце проведення: ")
                .append(ITALIC)
                .append(competition.getCountry())
                .append(", ")
                .append(BOLD)
                .append(competition.getCity())
                .append(BOLD);
        return sb;
    }

    private StringBuilder competitionDate(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        CompetitionStatus status = CompetitionStatus.getByName(competition.getStatus());
        Icon iconForStatus = getIconForStatus(status);
        sb
                .append(CALENDAR)
                .append(ITALIC)
                .append(" Дата: ")
                .append(ITALIC)
                .append(competition.getBeginDate())
                .append(" - ")
                .append(competition.getEndDate())
                .append(" ")
                .append(iconForStatus);
        return sb;
    }

    private Icon getIconForStatus(CompetitionStatus status) {
        if (status == null) return null;
        return switch (status) {
            case CANCELED -> GRAY_CIRCLE;
            case PLANED -> BLUE_CIRCLE;
            case NOT_STARTED -> GREEN_CIRCLE;
            case IN_PROGRESS -> RED_CIRCLE;
            case FINISHED -> LIGHT_GRAY_CIRCLE;
        };
    }

    private StringBuilder competitionName(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(ITALIC)
                .append(competition.getName())
                .append(ITALIC);
        return sb;
    }

    private void sendTypingAction(String chatId) {
        log.info("sendTyping for chat: " + chatId);
        SendChatAction chatAction = SERVICE.getChatAction(chatId);
        publisher.publishEvent(new SendMessageEvent(this, chatAction, CHAT_ACTION));
    }

    private SendMessageEvent getMessageForCompetitions(List<CompetitionEntity> competitions, String
            chatId, Integer messageId, int page) {
        if (page == 0) {
            LocalDateTime now = LocalDateTime.now();
            Map<Integer, Integer> compareMap = new HashMap<>();
            for (int i = 0; i < competitions.size(); i++) {
                CompetitionEntity competition = competitions.get(i);
                LocalDateTime competitionDate = LocalDateTime.parse(
                        String.format("%s %s",
                                competition.getBeginDate(), now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
                int abs = Math.abs(now.compareTo(competitionDate));
                compareMap.put(abs, i);
            }
            int index = compareMap.get(Collections.min(compareMap.keySet()));
            page = index / COMPETITIONS_PAGE_SIZE;
        }
        List<CompetitionEntity> pageList = getPage(competitions, page, COMPETITIONS_PAGE_SIZE);

        int totalCompetitions = competitions.size();
        String text = getText(pageList, page, totalCompetitions);
        InlineKeyboardMarkup keyboard = getKeyboard(pageList, page, totalCompetitions);

        return prepareSendMessageEvent(chatId, messageId, text, keyboard);
    }

    private InlineKeyboardMarkup getKeyboard(List<CompetitionEntity> competitions, int page, int totalSize) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(getRowWithPages(page, totalSize));

        List<InlineKeyboardButton> row = new ArrayList<>();
        IntStream.range(0, competitions.size()).forEach(i -> {
            CompetitionEntity competition = competitions.get(i);
            int count = i + 1;
            InlineKeyboardButton button = SERVICE.getKeyboardButton(
                    getShortName(Icon.getIconForNumber(count).toString(), COMPETITION_BUTTON_LIMIT),
                    "/competition " + competition.getId()
            );
            row.add(button);
        });
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private List<InlineKeyboardButton> getRowWithPages(int page, int totalSize) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (page > 0) {
            InlineKeyboardButton leftPage = SERVICE.getKeyboardButton(
                    PREVIOUS.toString(),
                    "/competitions " + (page - 1)
            );
            row.add(leftPage);
        }
        if (page < getTotalPages(totalSize) - 1) {
            InlineKeyboardButton rightPage = SERVICE.getKeyboardButton(
                    NEXT.toString(),
                    "/competitions " + (page + 1)
            );
            row.add(rightPage);
        }
        return row;
    }

    private static int getTotalPages(int totalSize) {
        return (int) Math.ceil((double) totalSize / COMPETITIONS_PAGE_SIZE);
    }


    private String getText(List<CompetitionEntity> competitions, int page, int totalCompetitions) {
        StringBuilder result = new StringBuilder();
        result
                .append(CHAMPIONSHIP)
                .append(BOLD)
                .append("Список змагань:")
                .append(BOLD)
                .append(END_LINE)
                .append(END_LINE);

        result.append(
                IntStream.range(0, competitions.size())
                        .mapToObj(i -> {
                            int count = i + 1;
                            StringBuilder sb = new StringBuilder();
                            sb.append(BOLD).append(Icon.getIconForNumber(count)).append(" ").append(BOLD); // number
                            CompetitionEntity competition = competitions.get(i);
                            sb
                                    .append(competitionName(competition))
                                    .append(END_LINE);
                            sb
                                    .append(competitionDate(competition))
                                    .append(END_LINE)
                                    .append(competitionArea(competition))
                                    .append("   ")
                                    .append(competitionLink(competition))
                                    .append(END_LINE);

                            return sb.toString();
                        })
                        .reduce((s1, s2) -> s1 + END_LINE + s2)
                        .orElse("Список пустий")
        );

        result.append(END_LINE).append(END_LINE);
        result
                .append(PAGE_WITH_CURL)
                .append(" Сторінка ")
                .append("(")
                .append(BOLD)
                .append(page + 1)
                .append(BOLD)
                .append(")")
                .append(" з ")
                .append(BOLD)
                .append(getTotalPages(totalCompetitions))
                .append(BOLD);

        result
                .append("     ")
                .append(ITALIC)
                .append("[оберіть змагання]")
                .append(ITALIC);

        return result.toString();
    }

    private String getShortName(String text, int limit) {
        if (limit == 0) {
            return text;
        }
        if (text.length() <= limit) {
            return text;
        }
        return SERVICE.cleanMarkdown(text)
                .substring(0, limit) +
                "...";
    }

    private List<CompetitionEntity> getPage(List<CompetitionEntity> competitions, int pageNumber, int pageSize) {
        int fromIndex = pageNumber * pageSize;
        int toIndex = Math.min((pageNumber + 1) * pageSize, competitions.size());

        if (fromIndex >= competitions.size() || fromIndex < 0 || toIndex < 0) {
            throw new IllegalArgumentException("Invalid page parameters");
        }
        return competitions.subList(fromIndex, toIndex);
    }

    private List<CompetitionEntity> getCompetitions() {
        CompetitionEntity competition = competitionService.getFreshestCompetition();

        if (isNeedToUpdate(competition)) {
            log.info("Need to update competitions");
            updateCompetitions();
        }
        log.info("Get competitions from DB");
        return competitionService.findAllOrderByBeginDate(true);
    }

    public void updateCompetitions() {
        List<CompetitionEntity> competitions = mainPageParser.parseCompetitions();
        competitions
                .forEach(c -> competitionService.saveOrUpdate(c));
        state.setUpdateCompetitionsTime(LocalDateTime.now());
    }

    private boolean isNeedToUpdate(CompetitionEntity competition) {
        if (competition == null) {
            return true;
        }
        LocalDateTime time = state.getUpdateCompetitionsTime();
        if (time == null) {
            return true;
        }
        return time.isBefore(LocalDateTime.now().minusMinutes(TTL_MINUTES_COMPETITION_UPDATE));
    }
}
