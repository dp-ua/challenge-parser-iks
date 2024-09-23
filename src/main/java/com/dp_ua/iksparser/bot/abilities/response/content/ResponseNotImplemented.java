package com.dp_ua.iksparser.bot.abilities.response.content;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.NOT_IMPLEMENTED;
import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
@Scope("prototype")
@ResponseTypeMarker(NOT_IMPLEMENTED)
@Slf4j
public class ResponseNotImplemented implements ResponseContentGenerator {
    @Override
    public String messageText(Object... args) {
        return Icon.WARNING +
                SPACE +
                BOLD +
                "Вибачте, цей функціонал ще знаходиться в стадії розробки" +
                BOLD +
                END_LINE +
                END_LINE +
                "Будь ласка, спробуйте пізніше";
    }

    @Override
    public InlineKeyboardMarkup keyboard(Object... args) {
        return SERVICE.getHideMessageKeyboard("OK");
    }
}
