package com.dp_ua.iksparser.exeption;

public class DuplicateCommandException extends RuntimeException {
    public DuplicateCommandException(String message) {
        super(message);
    }
}
