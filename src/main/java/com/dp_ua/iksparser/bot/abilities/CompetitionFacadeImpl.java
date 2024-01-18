package com.dp_ua.iksparser.bot.abilities;

import com.dp_ua.iksparser.bot.performer.event.SendMessageEvent;
import com.dp_ua.iksparser.dba.CompetitionService;
import com.dp_ua.iksparser.element.Competition;
import com.dp_ua.iksparser.service.Downloader;
import com.dp_ua.iksparser.service.MainPageParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static com.dp_ua.iksparser.bot.performer.event.SendMessageEvent.MsgType.CHAT_ACTION;
import static com.dp_ua.iksparser.bot.performer.event.SendMessageEvent.MsgType.SEND_MESSAGE;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
@Slf4j
public class CompetitionFacadeImpl implements CompetitionFacade {
    public static final String URL = "https://iks.org.ua/";
    public static final String COMPETITIONS_PAGE = "/competitions1/";
    public static final int COMPETITIONS_PAGE_SIZE = 5;
    public static final int COMPETITION_NAME_LIMIT = 55;
    public static final int TTL_MINUTES_COMPETITION_UPDATE = 10;
    public static final int FIRST_PAGE = 0;
    @Autowired
    Downloader downloader = new Downloader();
    @Autowired
    MainPageParser mainPageParser;
    @Autowired
    CompetitionService competitionService;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    public void showCompetitions(String chatId) {
        log.info("showCompetitions for chat: " + chatId);
        sendTyping(chatId);

        List<Competition> competitions = getCompetitions();
        List<Competition> page = getPage(competitions, FIRST_PAGE, COMPETITIONS_PAGE_SIZE);

        SendMessage message = getMessage(page, chatId);
        SendMessageEvent sendMessageEvent = new SendMessageEvent(this, message, SEND_MESSAGE);

        publisher.publishEvent(sendMessageEvent);
    }

    private void sendTyping(String chatId) {
        log.info("sendTyping for chat: " + chatId);
        SendChatAction chatAction = SERVICE.getChatAction(chatId);
        publisher.publishEvent(new SendMessageEvent(this, chatAction, CHAT_ACTION));
    }

    private SendMessage getMessage(List<Competition> competitions, String chatId) {
        String text = getText(competitions);

        SendMessage message = SERVICE.getSendMessage(
                chatId,
                text,
                null,
                true
        );
        message.disableWebPagePreview();
        return message;
    }


    private String getText(List<Competition> competitions) {

        String result = BOLD + "Список змагань:" + BOLD + END_LINE +
                IntStream.range(0, competitions.size())
                        .mapToObj(i -> {
                            StringBuilder sb = new StringBuilder();
                            sb.append(BOLD).append(i + 1).append(". ").append(BOLD); // number
                            Competition competition = competitions.get(i);
                            sb
                                    .append(ITALIC)
                                    .append(getShortName(competition.getName()))
                                    .append(ITALIC);

                            sb.append(END_LINE);

                            sb
                                    .append("   з ")
                                    .append(competition.getBeginDate())
                                    .append(" по ") // begin date
                                    .append(competition.getEndDate()); // e

                            sb
                                    .append(LINK)
                                    .append("\uD83D\uDD17")
                                    .append(LINK_END)
                                    .append(LINK_SEPARATOR)
                                    .append(URL).append(competition.getUrl()) // URL;
                                    .append(LINK_SEPARATOR_END);

                            return sb.toString();
                        })
                        .reduce((s1, s2) -> s1 + END_LINE + s2)
                        .orElse("Список пустий");

        return result;
    }

    private static String getShortName(String text) {
        return SERVICE.cleanMarkdown(text)
                .substring(0, Math.min(text.length(), COMPETITION_NAME_LIMIT)) +
                "...";
    }

    private List<Competition> getPage(List<Competition> competitions, int pageNumber, int pageSize) {
        int fromIndex = pageNumber * pageSize;
        int toIndex = Math.min((pageNumber + 1) * pageSize, competitions.size());

        if (fromIndex >= competitions.size() || fromIndex < 0 || toIndex < 0) {
            throw new IllegalArgumentException("Invalid page parameters");
        }
        return competitions.subList(fromIndex, toIndex);
    }


    private List<Competition> getCompetitions() {
        Competition competition = competitionService.getFreshestCompetition();

        if (isNeedToUpdate(competition)) {
            log.info("Need to update competitions");
            updateCompetitions();
        }
        log.info("Get competitions from DB");
        return competitionService.findAllOrderByBeginDate(true);
    }

    private void updateCompetitions() {
        Document document = downloader.getDocument(URL + COMPETITIONS_PAGE);
        List<Competition> competitions = mainPageParser.getParsedCompetitions(document);
        competitions
                .forEach(c -> {
                    Competition competition = competitionService.saveOrUpdate(c);
                    log.info("Competition: " + competition);
                });
    }

    private boolean isNeedToUpdate(Competition competition) {
        if (competition == null) {
            return true;
        }
        return competition.getUpdatedTime().isBefore(LocalDateTime.now().minusMinutes(TTL_MINUTES_COMPETITION_UPDATE));
    }
}
