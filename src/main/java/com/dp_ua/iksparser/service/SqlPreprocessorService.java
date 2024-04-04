package com.dp_ua.iksparser.service;

import org.springframework.stereotype.Service;

@Service
public class SqlPreprocessorService {

    public String escapeSpecialCharacters(String input) {
        return input.replace("%", "\\%").replace("_", "\\_");
    }
}