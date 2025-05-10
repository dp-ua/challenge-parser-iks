package com.dp_ua.iksparser.bot.command;

import lombok.Getter;

@Getter
public enum CommandArgumentName {
    PARTICIPANT_ID("pi"),
    COMPETITION_ID("ci"),
    PAGE("pg"),
    ACTION("a"),
    SEARCH("sh"),
    ;
    private final String value;

    CommandArgumentName(String value) {
        this.value = value;
    }
}