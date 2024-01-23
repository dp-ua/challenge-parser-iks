package com.dp_ua.iksparser.bot.abilities;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.command.impl.CommandSearchByNameWithName;
import com.dp_ua.iksparser.bot.performer.event.SendMessageEvent;
import com.dp_ua.iksparser.dba.element.*;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.service.Downloader;
import com.dp_ua.iksparser.service.JsonReader;
import com.dp_ua.iksparser.service.MainPageParser;
import com.dp_ua.iksparser.service.UpdateCompetitionEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dp_ua.iksparser.bot.Icon.*;
import static com.dp_ua.iksparser.bot.performer.event.SendMessageEvent.MsgType.*;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
@Slf4j
public class CompetitionFacadeImpl implements CompetitionFacade {
    public static final String URL = "https://iks.org.ua";
    public static final String COMPETITIONS_PAGE = "/competitions1/";
    public static final int COMPETITIONS_PAGE_SIZE = 4;
    public static final int COMPETITION_NAME_LIMIT = 100;
    private static final int COMPETITION_BUTTON_LIMIT = 40;
    public static final int TTL_MINUTES_COMPETITION_UPDATE = 10;
    public static final int MAX_PARTICIPANTS_SIZE_TO_FIND = 5;

    @Autowired
    Downloader downloader;
    @Autowired
    MainPageParser mainPageParser;
    @Autowired
    CompetitionService competitionService;
    @Autowired
    ApplicationEventPublisher publisher;
    @Autowired
    StateService stateService;
    @Autowired
    JsonReader jSonReader;

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
        stateService.setState(chatId, CommandSearchByNameWithName.getTextForState(competition.getId().toString()));
        StringBuilder sb = getFindByNameMessage(competition);
        publishEvent(prepareSendMessageEvent(
                chatId,
                editMessageId,
                sb.toString(),
                getBackToCompetitionsKeyboard()
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
                .collect(Collectors.toList());
        if (heatLines.isEmpty()) {
            log.warn("No participants found");
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Учасників не знайдено", getBackToCompetitionsKeyboard()));
            return;
        }
        //collect participants from heatLines to set
        Set<ParticipantEntity> participants = heatLines.stream()
                .map(HeatLineEntity::getParticipant)
                .collect(Collectors.toSet());
        if (participants.size() > MAX_PARTICIPANTS_SIZE_TO_FIND) {
            log.warn("More than one participant found");
            publishEvent(prepareSendMessageEvent(
                    chatId, editMessageId,
                    "Знайдено забагато спортсменів. Введіть повніше прізвище", getBackToCompetitionsKeyboard()));
            return;
        }
        participants.forEach(participant -> {
            StringBuilder sb = new StringBuilder();
            sb

                    .append(LOOK)
                    .append(BOLD)
                    .append("Знайдено спортсмена: ")
                    .append(BOLD)
                    .append(END_LINE)
                    .append(LINK)
                    .append(ATHLETE)
                    .append(participant.getSurname())
                    .append(" ")
                    .append(participant.getName())
                    .append(" ")
                    .append(Icon.URL)
                    .append(LINK_END)
                    .append(LINK_SEPARATOR)
                    .append(participant.getUrl())
                    .append(LINK_SEPARATOR_END)
                    .append(END_LINE);
            sb
                    .append(participant.getRegion())
                    .append(", ")
                    .append(participant.getTeam())
                    .append(END_LINE);
            sb
                    .append(BIRTHDAY)
                    .append("Дата народження: ")
                    .append(participant.getBorn())
                    .append(END_LINE)
                    .append(END_LINE);


            sb
                    .append(ITALIC)
                    .append(competition.getName())
                    .append(ITALIC)
                    .append(END_LINE)
                    .append(HEAT)
                    .append("Приймає участь у змаганнях: ")
                    .append(END_LINE);

            heatLines.forEach(heatLine -> {
                HeatEntity heat = heatLine.getHeat();
                EventEntity event = heat.getEvent();
                DayEntity day = event.getDay();
                sb
                        .append(MARK)
                        .append(" ")
                        .append(day.getDayName())
                        .append(", ")
                        .append(event.getTime())
                        .append(", ")
                        .append(event.getEventName())
                        .append(", ")
                        .append(event.getRound())
                        .append(", ")
                        .append(heat.getName())
                        .append(", д.")
                        .append(heatLine.getLane())
                        .append(",bib.")
                        .append(heatLine.getBib())
                        .append(END_LINE);
            });

            publishEvent(prepareSendMessageEvent(
                    chatId,
                    null,
                    sb.toString(),
                    getBackToCompetitionKeyboard(competitionId)
            ));
        });

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
                .append(FIND)
                .append("Пошук участі спортсмена буде проводитись в івенті: ")
                .append(competitionName(competition))
                .append(competitionDate(competition))
                .append(competitionArea(competition));
        return sb;
    }

    private void publishUpdateCompetitionMessage(String chatId, Integer editMessageId, CompetitionEntity competition) {
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
        }
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private List<InlineKeyboardButton> lookAtCompetitionByNameButton(CompetitionEntity competition) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                FIND + " Пошук за прізвищем спортсмена",
                "/searchbyname " + competition.getId()
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
                .append(END_LINE);

        sb
                .append(competitionDate(competition))
                .append(END_LINE);

        sb
                .append(competitionArea(competition))
                .append(END_LINE);

        sb
                .append(competitionLink(competition))
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
                .append(END_LINE)
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
        sb
                .append(LINK)
                .append(Icon.URL)
                .append(" Посилання на сайт змагань ")
                .append(Icon.URL)
                .append(LINK_END)
                .append(LINK_SEPARATOR)
                .append(competition.getUrl())
                .append(LINK_SEPARATOR_END)
                .append(END_LINE);
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
                .append(competition.getCity())
                .append(END_LINE);
        return sb;
    }

    private StringBuilder competitionDate(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(CALENDAR)
                .append(ITALIC)
                .append(" Дати проведення: ")
                .append(ITALIC)
                .append(competition.getBeginDate())
                .append(" - ")
                .append(competition.getEndDate())
                .append(END_LINE);
        return sb;
    }

    private StringBuilder competitionName(CompetitionEntity competition) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(BOLD)
                .append(competition.getName())
                .append(BOLD)
                .append(END_LINE);
        return sb;
    }

    private void sendTypingAction(String chatId) {
        log.info("sendTyping for chat: " + chatId);
        SendChatAction chatAction = SERVICE.getChatAction(chatId);
        publisher.publishEvent(new SendMessageEvent(this, chatAction, CHAT_ACTION));
    }

    private SendMessageEvent getMessageForCompetitions(List<CompetitionEntity> competitions, String
            chatId, Integer messageId, int page) {
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

        IntStream.range(0, competitions.size()).forEachOrdered(i -> {
            List<InlineKeyboardButton> row = new ArrayList<>();
            CompetitionEntity competition = competitions.get(i);
            int count = i + 1;
            InlineKeyboardButton button = SERVICE.getKeyboardButton(
                    getShortName(Icon.getIconForNumber(count) + "  " + competition.getName(), COMPETITION_BUTTON_LIMIT),
                    "/competition " + competition.getId()
            );
            row.add(button);
            rows.add(row);
        });

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
                            StringBuilder sb = new StringBuilder();
                            sb.append(BOLD).append(i + 1).append(". ").append(BOLD); // number
                            CompetitionEntity competition = competitions.get(i);
                            sb
                                    .append(ITALIC)
                                    .append(getShortName(competition.getName(), COMPETITION_NAME_LIMIT))
                                    .append(ITALIC);

                            sb.append(END_LINE);

                            sb
                                    .append(CALENDAR)
                                    .append("  з ")
                                    .append(competition.getBeginDate())
                                    .append(" по ") // begin date
                                    .append(competition.getEndDate());
                            sb
                                    .append(" ")
                                    .append(AREA)
                                    .append(BOLD)
                                    .append(competition.getCity()) // place
                                    .append(BOLD);

                            sb
                                    .append(LINK)
                                    .append(CHAIN)
                                    .append(LINK_END)
                                    .append(LINK_SEPARATOR)
                                    .append(competition.getUrl()) // URL;
                                    .append(LINK_SEPARATOR_END);

                            return sb.toString();
                        })
                        .reduce((s1, s2) -> s1 + END_LINE + s2)
                        .orElse("Список пустий")
        );

        result.append(END_LINE).append(END_LINE);
        result
                .append(PAGE_WITH_CURL)
                .append(" Сторінка ")
                .append(BOLD)
                .append(page + 1)
                .append(BOLD)
                .append(" з ")
                .append(getTotalPages(totalCompetitions));

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
                .substring(0, Math.min(text.length(), limit)) +
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

    private void updateCompetitions() {
        Document document = downloader.getDocument(URL + COMPETITIONS_PAGE);
        List<CompetitionEntity> competitions = mainPageParser.getParsedCompetitions(document);
        competitions
                .forEach(c -> {
                    c.setUrl(URL + c.getUrl());
                    CompetitionEntity competition = competitionService.saveOrUpdate(c);
                    log.info("Competition: " + competition);
                });
    }

    private boolean isNeedToUpdate(CompetitionEntity competition) {
        if (competition == null) {
            return true;
        }
        return competition.getUpdatedTime().isBefore(LocalDateTime.now().minusMinutes(TTL_MINUTES_COMPETITION_UPDATE));
    }
}
