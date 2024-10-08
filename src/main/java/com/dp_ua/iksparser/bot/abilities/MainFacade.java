package com.dp_ua.iksparser.bot.abilities;

public interface MainFacade {
    void menu(String chatId, String argument, Integer editMessageId);

    void start(String chatId);
}
