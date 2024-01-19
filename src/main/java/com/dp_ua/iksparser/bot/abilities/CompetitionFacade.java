package com.dp_ua.iksparser.bot.abilities;

public interface CompetitionFacade {
    void showCompetitions(String chatId, int page, Integer editMessageId);

    void showCompetition(String chatId, int commandArgument, Integer editMessageId);
}
