package com.dp_ua.iksparser.bot.abilities.response;

import org.springframework.data.domain.Page;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Optional;

import static com.dp_ua.iksparser.service.MessageCreator.BOLD;

public interface ResponseContentGenerator {
    default ResponseContainer getContainer(Object... args) {
        return new ResponseContainer(messageText(args), keyboard(args));
    }

    String messageText(Object... args);

    InlineKeyboardMarkup keyboard(Object... args);

    default void validate(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    default Optional<String> getArgument(int index, Object... args) {
        if (args.length > index) {
            return Optional.of(args[index].toString());
        }
        return Optional.empty();
    }

    default Optional<?> getArgumentObject(int index, Object... args) {
        if (args.length > index) {
            return Optional.of(args[index]);
        }
        return Optional.empty();
    }

    default String getPageInfoNavigation(Page<?> page) {
        String sb = "Cторінка " +
                BOLD +
                (page.getNumber() + 1) +
                BOLD +
                " із " +
                BOLD +
                page.getTotalPages() +
                BOLD;
        return sb;
    }
}