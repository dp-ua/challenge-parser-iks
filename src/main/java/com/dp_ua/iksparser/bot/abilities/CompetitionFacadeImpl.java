package com.dp_ua.iksparser.bot.abilities;

import com.dp_ua.iksparser.element.Competition;
import com.dp_ua.iksparser.service.Downloader;
import com.dp_ua.iksparser.service.MainPageParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class CompetitionFacadeImpl implements CompetitionFacade {
    public static final String URL = "https://iks.org.ua/";
    public static final String COMPETITIONS_PAGE = "/competitions1/";
    @Autowired
    Downloader downloader = new Downloader();
    @Autowired
    MainPageParser mainPageParser;

    @Override
    public void showCompetitions(String chatId) {
        log.info("showCompetitions for chat: " + chatId);
        Document document = downloader.getDocument(URL + COMPETITIONS_PAGE);
        List<Competition> competitions = mainPageParser.getParsedCompetitions(document);
        competitions.stream().filter(c -> c.getBegin().isAfter(LocalDate.now().minusDays(5)))
                .forEach(c -> log.info(c.toString()));
        // todo send message to chatId
    }
}
