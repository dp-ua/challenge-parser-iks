package com.dp_ua.iksparser.bot.abilities.response.content.search;

import com.dp_ua.iksparser.bot.abilities.infoview.SearchView;
import com.dp_ua.iksparser.bot.abilities.infoview.SubscriptionView;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ResponseFindParticipantInCompetition.class)
public class ResponseFindParticipantInCompetitionTest {
    @Autowired
    ResponseFindParticipantInCompetition response;
    @MockBean
    SearchView searchView;
    @MockBean
    SubscriptionView subscriptionView;

    ParticipantEntity participant = mock(ParticipantEntity.class);
    CompetitionEntity competition = mock(CompetitionEntity.class);

    @Test
    public void shouldAcceptArgs_AndNotThrowException() {
        prepareMocks();
        List<HeatLineEntity> heatLines = List.of(new HeatLineEntity());
        boolean subscribed = true;
        assertDoesNotThrow(() -> response.getContainer(participant, competition, heatLines, subscribed));
    }

    private void prepareMocks() {
        when(participant.getId()).thenReturn(1L);
        when(competition.getId()).thenReturn(1L);
        when(subscriptionView.buttonUnsubscribe(any())).thenReturn(mock(InlineKeyboardButton.class));
        when(searchView.foundParticipantInCompetitionMessage(any(), any(), any())).thenReturn("message");
    }
}