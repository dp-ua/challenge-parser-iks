package com.dp_ua.iksparser.bot.abilities.response.content.basic;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.MENU;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import com.dp_ua.iksparser.bot.abilities.infoview.MenuView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;

import lombok.RequiredArgsConstructor;

@Component
@Scope("prototype")
@ResponseTypeMarker(MENU)
@RequiredArgsConstructor
public class ResponseMenu implements ResponseContentGenerator {

    private static final int ARGS_COMPETITIONS_INFO_INDEX = 0;
    private static final int ARGS_PARTICIPANTS_INFO_INDEX = 1;
    private static final int ARGS_SUBSCRIBE_INFO_INDEX = 2;

    private final MenuView menuView;

    @Override
    public String messageText(Object... args) {
        String competitionsInfo = getArgument(ARGS_COMPETITIONS_INFO_INDEX, args).orElseThrow();
        String participantsInfo = getArgument(ARGS_PARTICIPANTS_INFO_INDEX, args).orElseThrow();
        String subscribeInfo = getArgument(ARGS_SUBSCRIBE_INFO_INDEX, args).orElseThrow();

        return menuView.mainMenu(competitionsInfo, participantsInfo, subscribeInfo);
    }

    @Override
    public InlineKeyboardMarkup keyboard(Object... args) {
        return menuView.menuButtons();
    }

}
