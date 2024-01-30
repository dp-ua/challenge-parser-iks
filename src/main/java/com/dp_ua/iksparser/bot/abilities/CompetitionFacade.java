package com.dp_ua.iksparser.bot.abilities;

public interface CompetitionFacade {
    void showCompetitions(String chatId, int page, Integer editMessageId);

    void showCompetition(String chatId, int commandArgument, Integer editMessageId);

    void startSearchByName(String chatId, int commandArgument, Integer editMessageId);

    void searchingByName(String chatId, String commandArgument, Integer editMessageId);

    void startSearchByCoach(String chatId, int commandArgument, Integer editMessageId);

    void searchingByCoachWithName(String chatId, String commandArgument, Integer editMessageId);

    void showNotLoadedInfo(String chatId, int commandArgument, Integer editMessageId);
}
