package com.dp_ua.iksparser.bot.abilities.response.content.basic;

import com.dp_ua.iksparser.bot.abilities.infoview.MenuView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.START;

@Component
@Scope("prototype")
@ResponseTypeMarker(START)
public class ResponseStart implements ResponseContentGenerator {
    @Autowired
    MenuView menuView;
    @Value("${telegram.bot.visibleName}")
    private String visibleName;
    @Value("${telegram.bot.name}")
    private String name;

    @Override
    public String messageText(Object... args) {
        return menuView.startText(visibleName, botURL());
    }

    @Override
    public InlineKeyboardMarkup keyboard(Object... args) {
        return menuView.startButtons();
    }

    private String botURL() {
        return "https://t.me/" + name;
    }
}
