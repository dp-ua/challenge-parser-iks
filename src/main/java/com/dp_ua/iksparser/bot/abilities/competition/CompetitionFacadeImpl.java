package com.dp_ua.iksparser.bot.abilities.competition;

import static com.dp_ua.iksparser.bot.Icon.ATHLETE;
import static com.dp_ua.iksparser.bot.Icon.COACH;
import static com.dp_ua.iksparser.bot.Icon.FIND;
import static com.dp_ua.iksparser.bot.Icon.NUMBER;
import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.FIND_PARTICIPANT_IN_COMPETITION;
import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.SEARCH_BY_BIB_NUMBER;
import static com.dp_ua.iksparser.configuration.Constants.BIB;
import static com.dp_ua.iksparser.configuration.Constants.COACH_NAME;
import static com.dp_ua.iksparser.configuration.Constants.COMPETITIONS_PAGE_SIZE;
import static com.dp_ua.iksparser.configuration.Constants.INPUT_BIB;
import static com.dp_ua.iksparser.configuration.Constants.INPUT_SURNAME;
import static com.dp_ua.iksparser.configuration.Constants.MAX_CHUNK_SIZE;
import static com.dp_ua.iksparser.configuration.Constants.MAX_PARTICIPANTS_SIZE_TO_FIND;
import static com.dp_ua.iksparser.configuration.Constants.NAME;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.dp_ua.iksparser.bot.abilities.FacadeMethods;
import com.dp_ua.iksparser.bot.abilities.infoview.CompetitionView;
import com.dp_ua.iksparser.bot.abilities.infoview.HeatLineView;
import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.infoview.SearchView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContainer;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.command.impl.CommandSearchByBibNumber;
import com.dp_ua.iksparser.bot.command.impl.CommandSearchByBibNumberWithBib;
import com.dp_ua.iksparser.bot.command.impl.CommandSearchByCoach;
import com.dp_ua.iksparser.bot.command.impl.CommandSearchByCoachWithName;
import com.dp_ua.iksparser.bot.command.impl.CommandSearchByName;
import com.dp_ua.iksparser.bot.command.impl.CommandSearchByNameWithName;
import com.dp_ua.iksparser.bot.command.impl.competition.CommandCompetitions;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.dba.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.entity.CoachEntity;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.CoachService;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.dba.service.HeatLineService;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import com.dp_ua.iksparser.exeption.ParsingException;
import com.dp_ua.iksparser.service.YearRange;
import com.dp_ua.iksparser.service.parser.MainParserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompetitionFacadeImpl extends FacadeMethods implements CompetitionFacade {

    private final SubscribeFacade subscribeFacade;
    private final MainParserService mainPageParser;
    private final CompetitionService competitionService;
    private final CoachService coachService;
    private final ParticipantService participantService;
    private final CompetitionView competitionView;
    private final HeatLineView heatLineView;
    private final ParticipantView participantView;
    private final SearchView searchView;
    private final HeatLineService heatLineService;

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
    public void showCompetition(String chatId, long competitionId, Integer editMessageId) {
        log.info("showCompetition. CommandArgument {}, chatId:{} ", competitionId, chatId);
        sendTypingAction(chatId);

        handleCompetition(chatId, competitionId, editMessageId, competition -> {
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
    public void startSearchByBibNumber(String chatId, long bibNumber, Integer editMessageId) {
        log.info("startSearchByBibNumber. CommandArgument {}, chatId:{} ", bibNumber, chatId);
        sendTypingAction(chatId);

        ResponseContentGenerator contentGenerator = contentFactory.getContentForResponse(SEARCH_BY_BIB_NUMBER);

        handleCompetition(chatId, bibNumber, editMessageId, competition -> {
            ResponseContainer content = contentGenerator.getContainer(competition);
            long competitionId = competition.getId();
            setStateForSearchingByBibNumber(chatId, competitionId);
            publishTextMessage(chatId, content.getMessageText(), content.getKeyboard());
        });
    }

    @Override
    public void startSearchByName(String chatId, long commandArgument, Integer editMessageId) {
        log.info("startSearchByName. CommandArgument {}, chatId:{} ", commandArgument, chatId);
        sendTypingAction(chatId);

        handleCompetition(chatId, commandArgument, editMessageId, competition -> {
            long competitionId = competition.getId();
            setStateForSearchingByName(chatId, competitionId);
            publishTextMessage(chatId, searchView.findByName(competition), competitionView.getBackToCompetitionKeyboard(competition));
        });
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
        sendTypingAction(chatId);

        long competitionId = parseID(commandArgument);
        String bib = parseBib(commandArgument);

        log.info("searchingByBibNumber. CompetitionId: {}, bib: {}, chatId: {}", competitionId, bib, chatId);

        CompetitionEntity competition = competitionService.findById(competitionId);

        setStateForSearchingByBibNumber(chatId, competitionId);

        if (isNotValidInputBibConditions(chatId, competition, competitionId, bib)) return;

        List<HeatLineEntity> heatLines = heatLineService.getHeatLinesInCompetitionByBib(competition.getId(), bib);

        if (heatLines.isEmpty()) {
            publishTextMessage(chatId, "Учасників з номером " + bib + " не знайдено", null);
        }

        Map<ParticipantEntity, List<HeatLineEntity>> separatedParticipantsAndHeatLines = getSeparatedParticipantsAndHeatLines(heatLines);

        if (separatedParticipantsAndHeatLines.size() > MAX_PARTICIPANTS_SIZE_TO_FIND) {
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Знайдено забагато спортсменів під цим номером",
                    competitionView.getBackToCompetitionKeyboard(competition))
            );
            publishFindMore(chatId, competition, INPUT_BIB);
            return;
        }

        separatedParticipantsAndHeatLines.forEach((participant, heatlines) ->
                showAthleteCompetitionParticipationDetails(chatId, null, participant, heatlines, competition));
        publishFindMore(chatId, competition, INPUT_BIB);
    }

    private void showAthleteCompetitionParticipationDetails(String chatId, Integer editMessageId,
                                                            ParticipantEntity participant,
                                                            List<HeatLineEntity> participantHeatLines,
                                                            CompetitionEntity competition) {
        boolean subscribed = subscribeFacade.isSubscribed(chatId, participant);

        ResponseContainer container = contentFactory.getContentForResponse(FIND_PARTICIPANT_IN_COMPETITION)
                .getContainer(participant, competition, participantHeatLines, subscribed);

        SendMessageEvent sendMessageEvent = prepareSendMessageEvent(chatId, editMessageId, container);

        publishEvent(sendMessageEvent);
    }

    @Override
    public void showAthleteCompetitionParticipationDetails(String chatId, Integer editMessageId,
                                                           long participantId, long competitionId) {
        log.info("showAthleteCompetitionParticipationDetails. ParticipantId: {}, competitionId: {}, chatId: {}", participantId, competitionId,
                chatId);

        Optional<ParticipantEntity> participant = participantService.findById(participantId);
        CompetitionEntity competition = competitionService.findById(competitionId);
        if (participant.isEmpty() || competition == null) {
            throw new IllegalArgumentException("Participant:%s or competition:%s not found"
                    .formatted(participantId, competitionId));
        }
        List<HeatLineEntity> heatlines = heatLineService.getHeatLinesInCompetitionByParticipantId(competitionId, participantId);

        showAthleteCompetitionParticipationDetails(chatId, editMessageId, participant.get(), heatlines, competition);
    }

    // todo move to utils
    // todo add normalisation and validation
    private long parseID(String commandArgument) {
        return Long.parseLong(jSonReader.getVal(commandArgument, "id"));
    }

    private String parseName(String commandArgument) {
        return jSonReader.getVal(commandArgument, NAME);
    }

    private String parseCoachName(String commandArgument) {
        return jSonReader.getVal(commandArgument, COACH_NAME);
    }

    private String parseBib(String commandArgument) {
        return jSonReader.getVal(commandArgument, BIB).trim();
    }


    @Override
    @Transactional(readOnly = true)
    public void searchingByName(String chatId, String commandArgument, Integer editMessageId) {
        sendTypingAction(chatId);

        long competitionId = parseID(commandArgument);
        String name = parseName(commandArgument);
        log.info("searchingByName. CompetitionId: {}, name: {}, chatId: {}", competitionId, name, chatId);

        CompetitionEntity competition = competitionService.findById(competitionId);
        setStateForSearchingByName(chatId, competitionId);

        if (isNotValidInputNameConditions(chatId, competition, competitionId, name)) return;

        List<HeatLineEntity> allHeatLines = heatLineService.getHeatLinesInCompetitionByParticipantSurname(
                competition.getId(),
                name
        );
        if (allHeatLines.isEmpty()) {
            publishTextMessage(chatId, "Учасників не знайдено", null);
            publishFindMore(chatId, competition, INPUT_SURNAME);
            return;
        }

        Map<ParticipantEntity, List<HeatLineEntity>> participantHeatLinesMap = getSeparatedParticipantsAndHeatLines(allHeatLines);

        if (participantHeatLinesMap.size() > MAX_PARTICIPANTS_SIZE_TO_FIND) {
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Знайдено забагато спортсменів. Введіть повніше прізвище",
                    competitionView.getBackToCompetitionKeyboard(competition))
            );
            publishFindMore(chatId, competition, INPUT_SURNAME);
            return;
        }

        participantHeatLinesMap.forEach((participant, heatLines) ->
                showAthleteCompetitionParticipationDetails(chatId, null, participant, heatLines, competition));
        publishFindMore(chatId, competition, INPUT_SURNAME);
    }

    private static Map<ParticipantEntity, List<HeatLineEntity>> getSeparatedParticipantsAndHeatLines(List<HeatLineEntity> heatLines) {
        Map<ParticipantEntity, List<HeatLineEntity>> participantHeatLinesMap = new HashMap<>();
        heatLines.forEach(heatLine -> {
            ParticipantEntity participant = heatLine.getParticipant();
            List<HeatLineEntity> participantHeatLines =
                    participantHeatLinesMap.computeIfAbsent(participant, k -> new ArrayList<>());
            participantHeatLines.add(heatLine);
        });
        return participantHeatLinesMap;
    }


    private void publishFindMore(String chatId, CompetitionEntity competition, String text) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
            Thread.currentThread().interrupt();
        }
        InlineKeyboardMarkup keyboard = competitionView.getBackToCompetitionKeyboard(competition);
        publishEvent(prepareSendMessageEvent(
                chatId,
                null,
                FIND + "Шукати ще?" +
                        END_LINE + END_LINE +
                        text,
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
            publishTextMessage(
                    chatId,
                    searchView.findByCoach(competition),
                    competitionView.getBackToCompetitionKeyboard(competition)
            );
        });
    }

    private void setStateSearchingByCoach(String chatId, long competitionId) {
        stateService.setState(chatId, CommandSearchByCoachWithName.getTextForState(competitionId));
    }


    @Override
    @Transactional(readOnly = true)
    public void searchingByCoachWithName(String chatId, String commandArgument, Integer editMessageId) {
        sendTypingAction(chatId);

        long competitionId = parseID(commandArgument);
        String coachName = parseCoachName(commandArgument);
        log.info("searchingByCoachWithName. CompetitionId: {}, coachName: {}, chatId: {}", competitionId, coachName, chatId);

        CompetitionEntity competition = competitionService.findById(competitionId);

        setStateSearchingByCoach(chatId, competitionId);

        if (isNotValidInputNameConditions(chatId, competition, competitionId, coachName)) return;

        List<CoachEntity> foundCoaches = coachService.searchByNamePartialMatch(coachName);
        if (isNotFoundCoaches(foundCoaches, coachName)) {
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Тренера не знайдено.", null));
            publishFindMore(chatId, competition, INPUT_SURNAME);
            return;
        }

        Map<CoachEntity, List<HeatLineEntity>> coachHeatLinesMap = new HashMap<>();
        foundCoaches.forEach(coach -> {
            List<HeatLineEntity> coachHeatLines = heatLineService.getHeatLinesInCompetitionWhereCoachIs(
                    competition.getId(),
                    coach.getId()
            );
            coachHeatLinesMap.put(coach, coachHeatLines);
        });
        if (coachHeatLinesMap.isEmpty()) {
            log.warn("No participants found");
            publishTextMessage(chatId, "Учасників не знайдено", null);
            publishFindMore(chatId, competition, INPUT_SURNAME);
            return;
        }
        coachHeatLinesMap.forEach((coach, coachHeatLines) -> {
            Map<ParticipantEntity, List<HeatLineEntity>> participantHeatLinesMap = getSeparatedParticipantsAndHeatLines(coachHeatLines);

            String header = searchView.foundParticipantHeader(coach, coachHeatLines, competition);
            List<StringBuilder> participantsInfo = getParticipantsInfo(participantHeatLinesMap);

            sendChunkedMessages(chatId, header, participantsInfo);
        });
        publishFindMore(chatId, competition, INPUT_SURNAME);
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
            heatLineView.heatLinesListInfo(participant, participantHeatLines)
                    .forEach(participantInfo::append);
            participantInfo.append(END_LINE);
            result.add(participantInfo);
        });
        return result;
    }

    @Override
    public void showNotLoadedInfo(String chatId, long competitionId, Integer editMessageId) {
        log.info("showNotLoadedInfo. CommandArgument {}, chatId:{} ", competitionId, chatId);
        sendTypingAction(chatId);

        handleCompetition(chatId, competitionId, editMessageId, competition -> {
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

    private boolean isNotValidInputBibConditions(String chatId, CompetitionEntity competition, long id, String bib) {
        if (isNotValidCompetition(chatId, competition, id)) return true;
        if (bib.isEmpty()) {
            log.warn("Bib is empty");
            publishTextMessage(chatId, "Ви не вказали біб-номер", null);
            publishFindMore(chatId, competition, INPUT_BIB);
            return true;
        }
        if (!bib.matches("\\d+")) {
            log.warn("Bib contains invalid symbols: {}", bib);
            publishTextMessage(chatId, "Ви вказали некоректний біб-номер", null);
            publishFindMore(chatId, competition, INPUT_BIB);
            return true;
        }
        return false;
    }

    private boolean isNotValidCompetition(String chatId, CompetitionEntity competition, long id) {
        if (competition == null) {
            log.warn("Competition[{}] not found", id);
            publishTextMessage(chatId, "Змагання не знайдено", getBackToCompetitionsKeyboard());
            return true;
        }
        return false;
    }

    private boolean isNotValidInputNameConditions(String chatId, CompetitionEntity competition, long id, String name) {
        if (isNotValidCompetition(chatId, competition, id)) return true;
        if (name.isEmpty()) {
            log.warn("Name is empty");
            publishTextMessage(chatId, "Ви не вказали прізвище", null);
            publishFindMore(chatId, competition, INPUT_SURNAME);
            return true;
        }
        if (name.length() < 3) {
            log.warn("Name is too short");
            publishTextMessage(chatId, "Ви вказали занадто коротке прізвище", null);
            publishFindMore(chatId, competition, INPUT_SURNAME);
            return true;
        }
        if (!name.matches("[а-яА-Яa-zA-Z-ґҐєЄіІїЇ']+")) {
            log.warn("Name contains invalid symbols: {}", name);
            publishTextMessage(chatId, "Ви вказали некоректне прізвище", null);
            publishFindMore(chatId, competition, INPUT_SURNAME);
            return true;
        }
        return false;
    }

    private InlineKeyboardMarkup getBackToCompetitionsKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = SERVICE.getBackButton(CommandCompetitions.getCallbackCommand());
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }


    private void publishEvent(SendMessageEvent messageEvent) {
        publisher.publishEvent(messageEvent);
    }

    private SendMessageEvent prepareSendMessageEvent(String chatId, Integer editMessageId, ResponseContainer content) {
        return prepareSendMessageEvent(chatId, editMessageId, content.getMessageText(), content.getKeyboard());
    }

    private SendMessageEvent prepareSendMessageEvent(String chatId, Integer editMessageId, String text, InlineKeyboardMarkup keyboard) {
        return SERVICE.getSendMessageEvent(chatId, text, keyboard, editMessageId);
    }

    private InlineKeyboardMarkup getCompetitionDetailsKeyboard(CompetitionEntity competition, boolean competitionFilled) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(SERVICE.getBackButton("/competitions"));
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
                CommandSearchByBibNumber.getCallbackCommand(competition.getId())
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
                .forEach(competitionService::saveOrUpdate);
        competitionService.flush();
        log.info("Competitions parsed. Size: {}, year: {}", competitions.size(), year);
        log.info("Competitions in DB: {}", competitionService.count());
    }

    @Override
    public String getInfoAboutCompetitions() {
        long allCount = competitionService.count();
        int filledCount = competitionService.getFilledCompetitions().size();
        YearRange yearRange = competitionService.getMinAndMaxYear();

        return competitionView.getCompetitionsInfo(allCount, filledCount, yearRange);
    }

    @Override
    public List<CompetitionDto> getCompetitionsForParticipant(ParticipantEntity participant) {
        return participant.getHeatLines().stream()
                .map(heatLine -> heatLine.getHeat().getEvent().getDay().getCompetition())
                .distinct()
                .map(competitionService::toDTO)
                .toList();
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
