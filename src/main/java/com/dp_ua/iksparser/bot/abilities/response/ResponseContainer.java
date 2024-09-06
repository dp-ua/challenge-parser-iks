package com.dp_ua.iksparser.bot.abilities.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Slf4j
@Getter
@AllArgsConstructor
public class ResponseContainer {
    private String messageText;
    private InlineKeyboardMarkup keyboard;
}
