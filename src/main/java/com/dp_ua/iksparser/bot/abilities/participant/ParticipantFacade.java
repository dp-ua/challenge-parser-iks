package com.dp_ua.iksparser.bot.abilities.participant;

public interface ParticipantFacade {
    void subscribe(String chatId, long commandArgument, Integer editMessageId);

    void unsubscribe(String chatId, long commandArgument, Integer editMessageId);

    String getInfoAboutParticipants();

    void showParticipants(String chatId, long commandArgument, Integer editMessageId);
}
