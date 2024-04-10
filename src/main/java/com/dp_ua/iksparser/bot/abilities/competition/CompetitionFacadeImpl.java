package com.dp_ua.iksparser.bot.abilities.competition;

import com.dp_ua.iksparser.bot.abilities.StateService;
import com.dp_ua.iksparser.bot.abilities.infoview.CompetitionView;
import com.dp_ua.iksparser.bot.abilities.infoview.HeatLineView;
import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.infoview.SubscriptionView;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.command.impl.*;
import com.dp_ua.iksparser.bot.command.impl.competition.CommandCompetitions;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.bot.event.UpdateCompetitionEvent;
import com.dp_ua.iksparser.dba.element.*;
import com.dp_ua.iksparser.dba.service.CoachService;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.exeption.ParsingException;
import com.dp_ua.iksparser.service.JsonReader;
import com.dp_ua.iksparser.service.PageableService;
import com.dp_ua.iksparser.service.parser.MainParserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.bot.event.SendMessageEvent.MsgType.CHAT_ACTION;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
@Slf4j
public class CompetitionFacadeImpl implements CompetitionFacade {
    public static final int COMPETITIONS_PAGE_SIZE = 3;
    public static final int MAX_PARTICIPANTS_SIZE_TO_FIND = 5;
    private static final int MAX_CHUNK_SIZE = 4096;

    @Autowired
    SubscribeFacade subscribeFacade;
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
    SubscriptionView subscriptionView;
    @Autowired
    CompetitionView competitionView;
    @Autowired
    HeatLineView heatLineView;
    @Autowired
    ParticipantView participantView;
    @Autowired
    PageableService pageableService;


    @Override
    public void showCompetitions(String chatId, int pageNumber, Integer editMessageId) {
        log.info("showCompetitions. Page {}, chatId:{} ", pageNumber, chatId);
        sendTypingAction(chatId);
        Page<CompetitionEntity> content = getCompetitionsPage(pageNumber);
        publishEvent(getMessageForCompetitions(content, chatId, editMessageId));
    }

    private Page<CompetitionEntity> getCompetitionsPage(int page) {
        if (page < 0) {
            return competitionService.getPagedCompetitionsClosetToDate(LocalDateTime.now(), COMPETITIONS_PAGE_SIZE);
        }
        return competitionService.getPagedCompetitions(page, COMPETITIONS_PAGE_SIZE);
    }

    @Override
    @Transactional
    public void showCompetition(String chatId, long commandArgument, Integer editMessageId) {
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

        StringBuilder sb = new StringBuilder();
        boolean competitionFilled = competition.isFilled();
        sb.append(competitionView.info(competition));
        if (competitionFilled) {
            sb
                    .append(competitionView.details(competition))
                    .append(END_LINE);
        } else {
            sb
                    .append(competitionView.notFilledInfo())
                    .append(updatingMessageText());
        }
        InlineKeyboardMarkup keyboard = getCompetitionDetailsKeyboard(competition, competitionFilled);

        publishEvent(prepareSendMessageEvent(
                chatId,
                editMessageId,
                sb.toString(),
                keyboard
        ));
        if (!competitionFilled) {
            log.info("Competition[{}] is not filled. Going to update", competition.getId());
            publishUpdateCompetitionMessage(chatId, editMessageId, competition);
        }
    }

    @Override
    public void startSearchByName(String chatId, long commandArgument, Integer editMessageId) {
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
        setStateForSearchingByName(chatId, competitionId);
        StringBuilder sb = getFindByNameMessage(competition);
        publishChunkMessage(chatId, sb, getBackToCompetitionKeyboard(competitionId));
    }

    private InlineKeyboardMarkup getEnoughKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                " Досить " + ENOUGH,
                "/" + CommandDeleteMessage.command
        );
        row.add(button);
        rows.add(row);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void setStateForSearchingByName(String chatId, String competitionId) {
        stateService.setState(chatId, CommandSearchByNameWithName.getTextForState(competitionId));
    }

    @Override
    @Transactional
    public void searchingByName(String chatId, String commandArgument, Integer editMessageId) {
        log.info("searchingByName. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        String competitionId = jSonReader.getVal(commandArgument, "id");
        String name = jSonReader.getVal(commandArgument, "name");

        CompetitionEntity competition = competitionService.findById(Integer.parseInt(competitionId));

        setStateForSearchingByName(chatId, competitionId);

        if (!isValidInputNameConditions(chatId, competition, competitionId, name)) return;

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
            publishChunkMessage(chatId, new StringBuilder("Учасників не знайдено"), null);
            publishFindMore(chatId, competitionId);
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
            publishFindMore(chatId, competitionId);
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
                    .append(participantView.info(participant))
                    .append(END_LINE)
                    .append(END_LINE);

            sb
                    .append(competitionView.nameAndDate(competition))
                    .append(END_LINE)
                    .append(END_LINE);
            sb
                    .append(HEAT)
                    .append("Приймає участь у змаганнях: ")
                    .append(END_LINE);

            heatLines.stream()
                    .filter(heatLine -> heatLine.getParticipant().equals(participant))
                    .forEach(heatLine -> sb.append(heatLineView.info(heatLine)));

            boolean subscribed = subscribeFacade.isSubscribed(chatId, participant);
            publishChunkMessage(chatId, sb, subscriptionView.button(participant, subscribed));
        });
        publishFindMore(chatId, competitionId);
    }

    private void publishFindMore(String chatId, String competitionId) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        InlineKeyboardMarkup keyboard = getBackToCompetitionKeyboard(competitionId);
//        keyboard.getKeyboard().addAll(getEnoughKeyboard().getKeyboard());
        publishEvent(prepareSendMessageEvent(
                chatId,
                null,
                FIND + "Шукати ще?\n\nВведіть прізвище",
                keyboard)
        );
    }

    @Override
    public void startSearchByCoach(String chatId, long commandArgument, Integer editMessageId) {
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
        setStateSearchingByCoach(chatId, competitionId);
        StringBuilder sb = getFindByCoachMessage(competition);
        publishChunkMessage(chatId, sb, getBackToCompetitionKeyboard(competitionId));
    }

    private void setStateSearchingByCoach(String chatId, String competitionId) {
        stateService.setState(chatId, CommandSearchByCoachWithName.getTextForState(competitionId));
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
                .append(competitionView.info(competition))
                .append(END_LINE);
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

        setStateSearchingByCoach(chatId, competitionId);

        if (!isValidInputNameConditions(chatId, competition, competitionId, coachName)) return;

        List<CoachEntity> foundCoaches = coachService.searchByNamePartialMatch(coachName);
        if (checkFoundCoaches(foundCoaches, coachName)) {
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Тренера не знайдено.", null));
            publishFindMore(chatId, competitionId);
            return;
        }

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
            publishChunkMessage(chatId, new StringBuilder("Учасників не знайдено"), null);
            publishFindMore(chatId, competitionId);
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
                    .append(competitionView.nameAndDate(competition))
                    .append(END_LINE)
                    .append(END_LINE);
            header
                    .append(ITALIC)
                    .append("Заявлені такі спортсмени: ")
                    .append(ITALIC)
                    .append(END_LINE)
                    .append(END_LINE);

            List<StringBuilder> participantsInfo = prepareParticipantsInfoList(participantHeatLinesMap);
            sendChunkedMessages(chatId, header, participantsInfo);
        });
        publishFindMore(chatId, competitionId);
    }

    private void sendChunkedMessages(String chatId, StringBuilder header, List<StringBuilder> participantsInfo) {
        StringBuilder message = new StringBuilder();
        message.append(header);
        participantsInfo.forEach(participantInfo -> {
            if (message.toString().length() + participantInfo.toString().length() >= MAX_CHUNK_SIZE) {
                publishChunkMessage(chatId, message, null);
                message.setLength(0);
                message.append(header);
            }
            message.append(participantInfo);
        });

        publishChunkMessage(chatId, message, null);
    }

    private void publishChunkMessage(String chatId, StringBuilder message, InlineKeyboardMarkup keyboard) {
        publishEvent(prepareSendMessageEvent(
                chatId,
                null,
                message.toString(),
                keyboard
        ));
    }

    private List<StringBuilder> prepareParticipantsInfoList(Map<ParticipantEntity, List<HeatLineEntity>> participantHeatLinesMap) {
        List<StringBuilder> result = new ArrayList<>();
        participantHeatLinesMap.forEach((participant, participantHeatLines) -> {
            StringBuilder participantInfo = new StringBuilder();
            participantInfo
                    .append(participantView.info(participant))
                    .append(END_LINE);
            participantHeatLines.forEach(heatLine -> participantInfo.append(heatLineView.info(heatLine)));
            participantInfo.append(END_LINE);
            result.add(participantInfo);
        });
        return result;
    }

    @Override
    public void showNotLoadedInfo(String chatId, long commandArgument, Integer editMessageId) {
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
        String sb = competitionView.info(competition) +
                END_LINE +
                competitionView.notFilledInfo();

        InlineKeyboardMarkup keyboard = getCompetitionDetailsKeyboard(competition, false);

        publishEvent(prepareSendMessageEvent(
                chatId,
                editMessageId,
                sb,
                keyboard
        ));
    }


    private boolean checkFoundCoaches(List<CoachEntity> foundCoaches, String coachName) {
        if (foundCoaches.isEmpty()) {
            log.warn("No coaches found: {}", coachName);
            return true;
        }
        return false;
    }

    private boolean isValidInputNameConditions(String chatId, CompetitionEntity competition, String id, String name) {
        if (competition == null) {
            log.warn("Competition[{}] not found", id);
            publishChunkMessage(chatId, new StringBuilder("Змагання не знайдено"), getBackToCompetitionsKeyboard());
            return false;
        }
        if (name.isEmpty()) {
            log.warn("Name is empty");
            publishChunkMessage(chatId, new StringBuilder("Ви не вказали прізвище"), null);
            publishFindMore(chatId, id);
            return false;
        }
        if (name.length() < 3) {
            log.warn("Name is too short");
            publishChunkMessage(chatId, new StringBuilder("Ви вказали занадто коротке прізвище"), null);
            publishFindMore(chatId, id);
            return false;
        }
        if (!name.matches("[а-яА-Яa-zA-Z-ґҐєЄіІїЇ']+")) {
            log.warn("Name contains invalid symbols: " + name);
            publishChunkMessage(chatId, new StringBuilder("Ви вказали некоректне прізвище"), null);
            publishFindMore(chatId, id);
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
                .append(competitionView.info(competition));
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

        List<InlineKeyboardButton> row = getBackButton("/" + CommandCompetitions.command);
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private InlineKeyboardMarkup getBackToCompetitionKeyboard(String competitionId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = getBackButton(
                "/" + CommandCompetitions.command + " " + competitionId
        );
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void publishEvent(SendMessageEvent chatId) {
        publisher.publishEvent(chatId);
    }

    private SendMessageEvent prepareSendMessageEvent(String chatId, Integer editMessageId, String text, InlineKeyboardMarkup keyboard) {
        return SERVICE.getSendMessageEvent(chatId, text, keyboard, editMessageId);
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

    private StringBuilder updatingMessageText() {
        StringBuilder sb = new StringBuilder();
        sb
                .append(END_LINE)
                .append(INFO)
                .append(" Оновлюю дані. Це може зайняти деякий час ")
                .append(INFO)
                .append(END_LINE)
                .append(END_LINE)
                .append("Вам прийде повідомлення, якщо дані вже доступні");
        return sb;
    }


    private void sendTypingAction(String chatId) {
        log.info("sendTyping for chat: " + chatId);
        SendChatAction chatAction = SERVICE.getChatAction(chatId);
        publisher.publishEvent(new SendMessageEvent(this, chatAction, CHAT_ACTION));
    }

    private SendMessageEvent getMessageForCompetitions(
            Page<CompetitionEntity> content, String chatId, Integer messageId) {

        String text = competitionView.getTextForCompetitionsPage(content);
        InlineKeyboardMarkup keyboard = competitionView.getKeyboardForCompetitionsPage(content);

        return prepareSendMessageEvent(chatId, messageId, text, keyboard);
    }

    private Page<CompetitionEntity> getPage(List<CompetitionEntity> competitions, long pageNumber, int pageSize) {
        return pageableService.getPage((int) pageNumber, pageSize, competitions);
    }

    private List<CompetitionEntity> getCompetitions() {
        log.info("Get competitions from DB");
        return competitionService.findAllOrderByBeginDateDesc();
    }

    @Override
    public synchronized void updateCompetitionsList(int year) throws ParsingException {
        log.info("Update competitions list. Year: {}", year);
        List<CompetitionEntity> competitions = mainPageParser.parseCompetitions(year);
        competitions
                .forEach(c -> competitionService.saveOrUpdate(c));
        competitionService.flush();
        log.info("Competitions parsed. Size: {}, year: {}", competitions.size(), year);
        log.info("Competitions in DB: {}", competitionService.count());
    }

    @Override
    public String getInfoAboutCompetitions() {
        List<CompetitionEntity> competitions = competitionService.findAllOrderByBeginDateDesc();
        List<String> years = competitions.stream().map(c -> {
            String beginDate = c.getBeginDate();
            LocalDate date = LocalDate.parse(beginDate, CompetitionService.FORMATTER);
            return date.getYear();
        }).distinct().sorted().map(String::valueOf).toList();

        return competitionView.getCompetitionsInfo(competitions, years);
    }
}
