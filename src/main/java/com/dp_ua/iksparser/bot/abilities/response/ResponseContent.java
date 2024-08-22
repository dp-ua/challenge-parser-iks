package com.dp_ua.iksparser.bot.abilities.response;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Optional;

public interface ResponseContent {
    String getMessageText(Object... args);

    InlineKeyboardMarkup getKeyboard(Object... args);

    default void validate(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    default Optional<String> getArgument(int index, Object... args) {
        if (args.length > index) {
            return (Optional<String>) args[index];
        }
        return Optional.empty();
    }

    default Optional<Object> getArgumentObject(int index, Object... args) {
        if (args.length > index) {
            return Optional.of(args[index]);
        }
        return Optional.empty();
    }
}