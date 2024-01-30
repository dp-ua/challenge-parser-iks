package com.dp_ua.iksparser.exeption;

import lombok.Getter;

public class ParsingException extends Throwable {
    @Getter
    private final ExceptionType type;

    public ParsingException(String text, ExceptionType type) {
        super(text);
        this.type = type;
    }
}
