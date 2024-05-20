package com.dp_ua.iksparser.bot.abilities.competition;

import com.dp_ua.iksparser.dba.dto.CompetitionDto;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.exeption.ParsingException;

import java.util.List;

public interface CompetitionFacade {
    void showCompetitions(String chatId, int page, Integer editMessageId) throws ParsingException;

    void showCompetition(String chatId, long commandArgument, Integer editMessageId);

    void startSearchByName(String chatId, long commandArgument, Integer editMessageId);

    void searchingByName(String chatId, String commandArgument, Integer editMessageId);

    void startSearchByCoach(String chatId, long commandArgument, Integer editMessageId);

    void searchingByCoachWithName(String chatId, String commandArgument, Integer editMessageId);

    void showNotLoadedInfo(String chatId, long commandArgument, Integer editMessageId);

    void updateCompetitionsList(int year) throws ParsingException;

    String getInfoAboutCompetitions();

    List<CompetitionDto> getCompetitionsForParticipant(ParticipantEntity participant);

    void startSearchByBibNumber(String chatId, long commandArgument, Integer editMessageId);

    void searchingByBibNumber(String chatId, String commandArgument, Integer editMessageId);
}
