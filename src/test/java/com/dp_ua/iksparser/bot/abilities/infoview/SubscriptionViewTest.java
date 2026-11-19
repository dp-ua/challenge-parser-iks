package com.dp_ua.iksparser.bot.abilities.infoview;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.EventEntity;
import com.dp_ua.iksparser.dba.entity.HeatEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

class SubscriptionViewTest {

    private static final String HAS_RESULT_TITLE = "Є результати змагання:";
    private static final String NEW_ENROLL_TITLE = "Нова заявка в івентах:";

    private HeatLineView heatLineView;
    private SubscriptionView view;

    @BeforeEach
    void setUp() {
        heatLineView = mock(HeatLineView.class);
        view = new SubscriptionView(mock(CompetitionView.class), heatLineView, mock(ParticipantView.class));
    }

    private HeatLineEntity heatLine(String resultUrl, String marker) {
        EventEntity event = new EventEntity();
        event.setResultUrl(resultUrl);
        HeatEntity heat = new HeatEntity();
        heat.setEvent(event);
        HeatLineEntity heatLine = new HeatLineEntity();
        heatLine.setHeat(heat);
        when(heatLineView.info(heatLine)).thenReturn(marker);
        return heatLine;
    }

    @Test
    void info_groupsHeatLinesByStatus_resultsFirst() {
        HeatLineEntity enrolled = heatLine("", "[ENROLLED]");
        HeatLineEntity withResult = heatLine("http://result", "[RESULT]");

        String text = view.info(new ParticipantEntity(), List.of(enrolled, withResult), new CompetitionEntity());

        int resultsTitle = text.indexOf(HAS_RESULT_TITLE);
        int enrollTitle = text.indexOf(NEW_ENROLL_TITLE);
        assertTrue(resultsTitle >= 0 && enrollTitle >= 0);
        assertTrue(resultsTitle < enrollTitle, "results group must come first");
        assertTrue(text.indexOf("[RESULT]") > resultsTitle && text.indexOf("[RESULT]") < enrollTitle);
        assertTrue(text.indexOf("[ENROLLED]") > enrollTitle);
    }

    @Test
    void info_skipsEmptyGroups() {
        HeatLineEntity enrolled = heatLine("", "[ENROLLED]");

        String text = view.info(new ParticipantEntity(), List.of(enrolled), new CompetitionEntity());

        assertTrue(text.contains(NEW_ENROLL_TITLE));
        assertFalse(text.contains(HAS_RESULT_TITLE));
    }

}
