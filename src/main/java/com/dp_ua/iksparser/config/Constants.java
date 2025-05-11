package com.dp_ua.iksparser.config;

import java.time.format.DateTimeFormatter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String DD_MM_YYYY = "dd.MM.yyyy";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DD_MM_YYYY);
    public static final int COMPETITIONS_PAGE_SIZE = 3;
    public static final int MAX_PARTICIPANTS_SIZE_TO_FIND = 5;
    public static final int MAX_CHUNK_SIZE = 4096;
    public static final String INPUT_SURNAME = "Введіть прізвище";
    public static final String INPUT_BIB = "Введіть номер учасника";
    public static final String COACH_NAME = "coachName";
    public static final String NAME = "name";
    public static final String BIB = "bib";

}
