package com.dp_ua.iksparser.bot.abilities;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.performer.event.SendMessageEvent;
import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.dba.element.UpdateStatusEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.service.Downloader;
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

    @Autowired
    Downloader downloader = new Downloader();
    @Autowired
    MainPageParser mainPageParser;
    @Autowired
    CompetitionService competitionService;
    @Autowired
    ApplicationEventPublisher publisher;

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

        List<InlineKeyboardButton> row = getBackButton();
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void publishEvent(SendMessageEvent chatId) {
        SendMessageEvent sendMessageEvent = chatId;
        publisher.publishEvent(sendMessageEvent);
    }

    private boolean isCompetitionFilled(CompetitionEntity competition) {
        return !competition.getDays().isEmpty();
    }

    private SendMessageEvent prepareSendMessageEvent(String chatId, Integer editMessageId, String text, InlineKeyboardMarkup keyboard) {
        SendMessageEvent sendMessageEvent;
        if (editMessageId == null) {
            SendMessage message = SERVICE.getSendMessage(
                    chatId,
                    text,
                    keyboard,
                    true
            );
            message.disableWebPagePreview();
            sendMessageEvent = new SendMessageEvent(this, message, SEND_MESSAGE);
        } else {
            EditMessageText editMessageText = SERVICE.getEditMessageText(
                    chatId,
                    editMessageId,
                    text,
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

        List<InlineKeyboardButton> row = getBackButton();
        rows.add(row);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private static List<InlineKeyboardButton> getBackButton() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = SERVICE.getKeyboardButton(
                "⬅️ Назад",
                "/competitions"
        );
        row.add(button);
        return row;
    }

    private String getCompetitionDetailsText(CompetitionEntity competition, boolean competitionFilled) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(BOLD)
                .append(competition.getName())
                .append(BOLD)
                .append(END_LINE)
                .append(END_LINE);
        sb.append(competition.getBeginDate())
                .append(" - ")
                .append(competition.getEndDate())
                .append(END_LINE)
                .append(END_LINE);
        sb.append(competition.getCountry())
                .append(", ")
                .append(competition.getCity())
                .append(END_LINE)
                .append(END_LINE);

        sb
                .append(LINK)
                .append("Посилання на сайт змагань ")
                .append(Icon.URL)
                .append(LINK_END)
                .append(LINK_SEPARATOR)
                .append(competition.getUrl())
                .append(LINK_SEPARATOR_END)
                .append(END_LINE)
                .append(END_LINE);

        if (competitionFilled) {
            sb
                    .append("Тривалість днів: ")
                    .append(BOLD)
                    .append(competition.getDays().size())
                    .append(BOLD)
                    .append(END_LINE);

            int participantsCount = competition.getDays().stream()
                    .flatMap(day -> day.getEvents().stream())
                    .flatMap(event -> event.getHeats().stream())
                    .flatMap(heap -> heap.getHeatLines().stream())
                    .map(heatLine -> heatLine.getParticipant())
                    .collect(Collectors.toSet()).size();
            sb
                    .append("Кількість учасників: ")
                    .append(BOLD)
                    .append(participantsCount)
                    .append(BOLD)
                    .append(END_LINE);

            int heatCount = competition.getDays().stream()
                    .flatMap(day -> day.getEvents().stream())
                    .flatMap(event -> event.getHeats().stream())
                    .collect(Collectors.toSet()).size();
            sb
                    .append("Кількість стартів: ")
                    .append(BOLD)
                    .append(heatCount)
                    .append(BOLD)
                    .append(END_LINE);
        } else {
            sb
                    .append(WARNING)
                    .append(" Інформація про змагання оновлюється ")
                    .append(WARNING)
                    .append(END_LINE)
                    .append(INFO)
                    .append(" Ви отримаєете повідомлення, коли інформація буде готова ")
                    .append(INFO)
                    .append(END_LINE);
        }
        return sb.toString();
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
        competitions
                .forEach(c -> {
                    List<InlineKeyboardButton> row = new ArrayList<>();
                    InlineKeyboardButton button = SERVICE.getKeyboardButton(
                            getShortName(c.getName(), COMPETITION_BUTTON_LIMIT),
                            "/competition " + c.getId()
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
                                    .append("   з ")
                                    .append(competition.getBeginDate())
                                    .append(" по ") // begin date
                                    .append(competition.getEndDate());
                            sb
                                    .append(" ")
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
                .append("Сторінка ")
                .append(BOLD)
                .append(page + 1)
                .append(BOLD)
                .append(" з ")
                .append(getTotalPages(totalCompetitions));

        return result.toString();
    }

    private String getShortName(String text, int limit) {
        if (limit==0) {
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
