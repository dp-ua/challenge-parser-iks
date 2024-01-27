package com.dp_ua.iksparser.service.parser;

import com.dp_ua.iksparser.dba.element.CompetitionEntity;

import java.util.List;

public interface MainParserService {
    List<CompetitionEntity> parseCompetitions();
}
