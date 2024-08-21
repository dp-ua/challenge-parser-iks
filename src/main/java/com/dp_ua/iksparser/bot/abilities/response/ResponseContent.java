package com.dp_ua.iksparser.bot.abilities.response;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface ResponseContent {
    String getMessageText(Object... args);

    InlineKeyboardMarkup getKeyboard(Object... args);
}