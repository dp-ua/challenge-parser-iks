package com.dp_ua.iksparser.service.parser;

import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.exeption.ParsingException;

import java.util.List;

public interface MainParserService {
    List<CompetitionEntity> parseCompetitions(int year) throws ParsingException;
}
