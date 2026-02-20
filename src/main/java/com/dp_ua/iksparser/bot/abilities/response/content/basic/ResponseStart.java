package com.dp_ua.iksparser.bot.abilities.response.content.basic;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.START;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import com.dp_ua.iksparser.bot.abilities.infoview.MenuView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import com.dp_ua.iksparser.configuration.TelegramBotProperties;

import lombok.RequiredArgsConstructor;

@Component
@Scope("prototype")
@ResponseTypeMarker(START)
@RequiredArgsConstructor
public class ResponseStart implements ResponseContentGenerator {

    private final MenuView menuView;
    private final TelegramBotProperties properties;

    @Override
    public String messageText(Object... args) {
        return menuView.startText(properties.getVisibleName(), botURL());
    }

    @Override
    public InlineKeyboardMarkup keyboard(Object... args) {
        return menuView.startButtons();
    }

    private String botURL() {
        return "https://t.me/" + properties.getName();
    }

}
