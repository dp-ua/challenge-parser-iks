package com.dp_ua.iksparser.bot.abilities.participant;

public interface ParticipantFacade {
    String getInfoAboutParticipants();

    void showParticipants(String chatId, long commandArgument, Integer editMessageId);

    void showFindAllParticipants(String chatId, String commandArgument, Integer editMessageId);

    void showParticipantDetails(String chatId, String commandArgument, Integer editMessageId);
}
