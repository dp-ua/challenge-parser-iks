package com.dp_ua.iksparser.bot.command;

public enum CommandArgumentName {
    PARTICIPANT_ID("pid"),
    COMPETITION_ID("cid"),
    PAGE("pg"),
    ID("id"),
    ACTION("act")
    ;
    private final String value;

    CommandArgumentName(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}