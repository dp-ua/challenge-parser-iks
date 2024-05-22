package com.dp_ua.iksparser.bot.abilities.competition;

import com.dp_ua.iksparser.bot.abilities.StateService;
import com.dp_ua.iksparser.bot.abilities.infoview.*;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.command.impl.*;
import com.dp_ua.iksparser.bot.command.impl.competition.CommandCompetition;
import com.dp_ua.iksparser.bot.command.impl.competition.CommandCompetitions;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.dba.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.entity.CoachEntity;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.CoachService;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.exeption.ParsingException;
import com.dp_ua.iksparser.service.JsonReader;
import com.dp_ua.iksparser.service.parser.MainParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.bot.event.SendMessageEvent.MsgType.CHAT_ACTION;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

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
    SearchView searchView;

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

        handleCompetition(chatId, commandArgument, editMessageId, competition -> {
            StringBuilder sb = new StringBuilder();
            boolean competitionFilled = competition.isFilled();
            sb.append(competitionView.info(competition));
            if (competitionFilled) {
                sb
                        .append(competitionView.details(competition))
                        .append(END_LINE);
            } else {
                sb
                        .append(competitionView.notFilledInfo());
            }
            InlineKeyboardMarkup keyboard = getCompetitionDetailsKeyboard(competition, competitionFilled);

            publishEvent(prepareSendMessageEvent(
                    chatId,
                    editMessageId,
                    sb.toString(),
                    keyboard
            ));
        });
    }

    @Override
    public void startSearchByBibNumber(String chatId, long commandArgument, Integer editMessageId) {
        log.info("startSearchByBibNumber. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        handleCompetition(chatId, commandArgument, editMessageId, competition -> {
            long competitionId = competition.getId();
            setStateForSearchingByBibNumber(chatId, competitionId);
            publishTextMessage(chatId, searchView.findByBibNumber(competition), getBackToCompetitionKeyboard(competitionId));
        });
    }

    @Override
    public void startSearchByName(String chatId, long commandArgument, Integer editMessageId) {
        log.info("startSearchByName. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        handleCompetition(chatId, commandArgument, editMessageId, competition -> {
            long competitionId = competition.getId();
            setStateForSearchingByName(chatId, competitionId);
            publishTextMessage(chatId, searchView.findByName(competition), getBackToCompetitionKeyboard(competitionId));
        });
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

    private void setStateForSearchingByBibNumber(String chatId, long competitionId) {
        stateService.setState(chatId, CommandSearchByBibNumberWithBib.getTextForState(competitionId));
    }

    private void setStateForSearchingByName(String chatId, long competitionId) {
        stateService.setState(chatId, CommandSearchByNameWithName.getTextForState(competitionId));
    }

    @Override
    @Transactional(readOnly = true)
    public void searchingByBibNumber(String chatId, String commandArgument, Integer editMessageId) {
        log.info("searchingByBibNumber. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        long competitionId = Long.parseLong(jSonReader.getVal(commandArgument, "id"));
        String bib = jSonReader.getVal(commandArgument, "bib").trim();

        CompetitionEntity competition = competitionService.findById(competitionId);

        setStateForSearchingByBibNumber(chatId, competitionId);

        if (!isValidInputBibConditions(chatId, competition, competitionId, bib)) return;

        List<HeatLineEntity> heatLines = competition.getDays().stream()
                .flatMap(day -> day.getEvents().stream())
                .flatMap(event -> event.getHeats().stream())
                .flatMap(heap -> heap.getHeatLines().stream())
                .filter(heatLine -> heatLine.getBib().equals(bib))
                .toList();

        if (heatLines.isEmpty()) {
            publishTextMessage(chatId, "Учасників з номером " + bib + " не знайдено", null);
        }
        Set<ParticipantEntity> participants = heatLines.stream()
                .map(HeatLineEntity::getParticipant)
                .collect(Collectors.toSet());
        if (participants.size() > MAX_PARTICIPANTS_SIZE_TO_FIND) {
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Знайдено забагато спортсменів під цим номером", getBackToCompetitionKeyboard(competitionId)));
            publishFindMore(chatId, competitionId);
            return;
        }
        participants.forEach(participant -> {
            String message = searchView.foundParticipantInCompetition(participant, competition, heatLines);
            boolean subscribed = subscribeFacade.isSubscribed(chatId, participant);
            publishTextMessage(chatId, message, subscriptionView.button(participant, subscribed));
        });
        publishFindMore(chatId, competitionId);
    }

    @Override
    @Transactional(readOnly = true)
    public void searchingByName(String chatId, String commandArgument, Integer editMessageId) {
        log.info("searchingByName. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        long competitionId = Long.parseLong(jSonReader.getVal(commandArgument, "id"));
        String name = jSonReader.getVal(commandArgument, "name");

        CompetitionEntity competition = competitionService.findById(competitionId);
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
            publishTextMessage(chatId, "Учасників не знайдено", null);
            publishFindMore(chatId, competitionId);
            return;
        }

        Set<ParticipantEntity> participants = heatLines.stream()
                .map(HeatLineEntity::getParticipant)
                .collect(Collectors.toSet());

        if (participants.size() > MAX_PARTICIPANTS_SIZE_TO_FIND) {
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Знайдено забагато спортсменів. Введіть повніше прізвище", getBackToCompetitionKeyboard(competitionId)));
            publishFindMore(chatId, competitionId);
            return;
        }

        participants.forEach(participant -> {
            String message = searchView.foundParticipantInCompetition(participant, competition, heatLines);
            boolean subscribed = subscribeFacade.isSubscribed(chatId, participant);
            publishTextMessage(chatId, message, subscriptionView.button(participant, subscribed));
        });
        publishFindMore(chatId, competitionId);
    }


    private void publishFindMore(String chatId, long competitionId) {
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

        handleCompetition(chatId, commandArgument, editMessageId, competition -> {
            long competitionId = competition.getId();
            setStateSearchingByCoach(chatId, competitionId);
            publishTextMessage(chatId, searchView.findByCoach(competition), getBackToCompetitionKeyboard(competitionId));
        });
    }

    private void setStateSearchingByCoach(String chatId, long competitionId) {
        stateService.setState(chatId, CommandSearchByCoachWithName.getTextForState(competitionId));
    }


    @Override
    @Transactional
    public void searchingByCoachWithName(String chatId, String commandArgument, Integer editMessageId) {
        log.info("searchingByCoachWithName. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        long competitionId = Long.parseLong(jSonReader.getVal(commandArgument, "id"));
        String coachName = jSonReader.getVal(commandArgument, "coachName");
        CompetitionEntity competition = competitionService.findById(competitionId);

        setStateSearchingByCoach(chatId, competitionId);

        if (!isValidInputNameConditions(chatId, competition, competitionId, coachName)) return;

        List<CoachEntity> foundCoaches = coachService.searchByNamePartialMatch(coachName);
        if (isNotFoundCoaches(foundCoaches, coachName)) {
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
            publishTextMessage(chatId, "Учасників не знайдено", null);
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

            String header = searchView.foundParticipantHeader(coach, coachHeatLines, competition);
            List<StringBuilder> participantsInfo = getParticipantsInfo(participantHeatLinesMap);

            sendChunkedMessages(chatId, header, participantsInfo);
        });
        publishFindMore(chatId, competitionId);
    }


    private void sendChunkedMessages(String chatId, String header, List<StringBuilder> participantsInfo) {
        StringBuilder message = new StringBuilder();
        message.append(header);
        participantsInfo.forEach(participantInfo -> {
            if (message.toString().length() + participantInfo.toString().length() >= MAX_CHUNK_SIZE) {
                publishTextMessage(chatId, message.toString(), null);
                message.setLength(0);
                message.append(header);
            }
            message.append(participantInfo);
        });

        publishTextMessage(chatId, message.toString(), null);
    }

    private void publishTextMessage(String chatId, String message, InlineKeyboardMarkup keyboard) {
        publishEvent(prepareSendMessageEvent(
                chatId,
                null,
                message,
                keyboard
        ));
    }

    private List<StringBuilder> getParticipantsInfo(Map<ParticipantEntity, List<HeatLineEntity>> participantHeatLinesMap) {
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

        handleCompetition(chatId, commandArgument, editMessageId, competition -> {
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
        });
    }


    private boolean isNotFoundCoaches(List<CoachEntity> foundCoaches, String coachName) {
        if (foundCoaches.isEmpty()) {
            log.info("No coaches found: {}", coachName);
            return true;
        }
        return false;
    }

    private boolean isValidInputBibConditions(String chatId, CompetitionEntity competition, long id, String bib) {
        if (isValidCompetition(chatId, competition, id)) return false;
        if (bib.isEmpty()) {
            log.warn("Bib is empty");
            publishTextMessage(chatId, "Ви не вказали біб-номер", null);
            publishFindMore(chatId, id);
            return false;
        }
        if (!bib.matches("[0-9]+")) {
            log.warn("Bib contains invalid symbols: {}", bib);
            publishTextMessage(chatId, "Ви вказали некоректний біб-номер", null);
            publishFindMore(chatId, id);
            return false;
        }
        return true;
    }

    private boolean isValidCompetition(String chatId, CompetitionEntity competition, long id) {
        if (competition == null) {
            log.warn("Competition[{}] not found", id);
            publishTextMessage(chatId, "Змагання не знайдено", getBackToCompetitionsKeyboard());
            return true;
        }
        return false;
    }

    private boolean isValidInputNameConditions(String chatId, CompetitionEntity competition, long id, String name) {
        if (isValidCompetition(chatId, competition, id)) return false;
        if (name.isEmpty()) {
            log.warn("Name is empty");
            publishTextMessage(chatId, "Ви не вказали прізвище", null);
            publishFindMore(chatId, id);
            return false;
        }
        if (name.length() < 3) {
            log.warn("Name is too short");
            publishTextMessage(chatId, "Ви вказали занадто коротке прізвище", null);
            publishFindMore(chatId, id);
            return false;
        }
        if (!name.matches("[а-яА-Яa-zA-Z-ґҐєЄіІїЇ']+")) {
            log.warn("Name contains invalid symbols: {}", name);
            publishTextMessage(chatId, "Ви вказали некоректне прізвище", null);
            publishFindMore(chatId, id);
            return false;
        }
        return true;
    }


    private InlineKeyboardMarkup getBackToCompetitionsKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = getBackButton("/" + CommandCompetitions.command);
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private InlineKeyboardMarkup getBackToCompetitionKeyboard(long competitionId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = getBackButton(
                "/" + CommandCompetition.command + " " + competitionId
        );
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void publishEvent(SendMessageEvent messageEvent) {
        publisher.publishEvent(messageEvent);
    }

    private SendMessageEvent prepareSendMessageEvent(String chatId, Integer editMessageId, String text, InlineKeyboardMarkup keyboard) {
        return SERVICE.getSendMessageEvent(chatId, text, keyboard, editMessageId);
    }

    private InlineKeyboardMarkup getCompetitionDetailsKeyboard(CompetitionEntity competition, boolean competitionFilled) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(getBackButton("/competitions"));
        if (competitionFilled) {
            rows.add(lookAtBibNumber(competition));
            rows.add(lookAtCompetitionByNameButton(competition));
            rows.add(lookAtCompetitionByCoachButton(competition));
        }
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private List<InlineKeyboardButton> lookAtBibNumber(CompetitionEntity competition) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                FIND + " Пошук спортсмена по номеру " + NUMBER,
                "/" + CommandSearchByBibNumber.command + " " + competition.getId()
        );
        row.add(button);
        return row;
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

    private void sendTypingAction(String chatId) {
        log.info("sendTyping for chat: {}", chatId);
        SendChatAction chatAction = SERVICE.getChatAction(chatId);
        publisher.publishEvent(new SendMessageEvent(this, chatAction, CHAT_ACTION));
    }

    private SendMessageEvent getMessageForCompetitions(
            Page<CompetitionEntity> content, String chatId, Integer messageId) {

        String text = competitionView.getTextForCompetitionsPage(content);
        InlineKeyboardMarkup keyboard = competitionView.getKeyboardForCompetitionsPage(content);

        return prepareSendMessageEvent(chatId, messageId, text, keyboard);
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
            LocalDate date = competitionService.getParsedDate(beginDate);
            return date.getYear();
        }).distinct().sorted().map(String::valueOf).toList();

        return competitionView.getCompetitionsInfo(competitions, years);
    }

    @Override
    public List<CompetitionDto> getCompetitionsForParticipant(ParticipantEntity participant) {
        return participant.getHeatLines().stream()
                .map(heatLine -> heatLine.getHeat().getEvent().getDay().getCompetition())
                .distinct()
                .map(c -> competitionService.convertToDto(c))
                .collect(Collectors.toList());
    }

    private void handleCompetition(String chatId, long competitionId,
                                   Integer editMessageId, Consumer<CompetitionEntity> callback) {
        CompetitionEntity competition = competitionService.findById(competitionId);
        if (competition == null) {
            log.info("Competition[{}] not found", competitionId);
            publishEvent(
                    prepareSendMessageEvent(chatId, editMessageId, "Змагання не знайдено", getBackToCompetitionsKeyboard())
            );
            return;
        }
        callback.accept(competition);
    }
}
