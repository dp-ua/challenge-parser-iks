package com.dp_ua.iksparser.bot.abilities.response.content;

import com.dp_ua.iksparser.bot.abilities.infoview.CompetitionView;
import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.infoview.SubscriptionView;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = {ResponseParticipantDetails.class})
@Slf4j
class ResponseParticipantDetailsTest {
    @Autowired
    ResponseParticipantDetails responseParticipantDetails;
    @MockBean
    ParticipantView participantView;
    @MockBean
    CompetitionView competitionView;
    @MockBean
    SubscriptionView subscriptionView;

    ParticipantEntity participant;
    boolean subscribed;
    Page<CompetitionEntity> competitionsPage;

    @BeforeEach
    public void setUp() {
        participant = mock(ParticipantEntity.class);
        subscribed = false;
        competitionsPage = mock(Page.class);
    }

    @Test
    public void shouldNotThrowException() {
        assertDoesNotThrow(
                () -> responseParticipantDetails.getContainer(participant, competitionsPage, subscribed),
                "Should accept 3 arguments and not throw exception"
        );
    }

    @Test
    public void shouldThrowException_LessArguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> responseParticipantDetails.getContainer(participant, subscribed),
                "Should throw exception if arguments are less than 3"
        );
    }

    @Test
    public void shouldThrowException_OtherOrder() {
        assertThrows(
                IllegalArgumentException.class,
                () -> responseParticipantDetails.getContainer(competitionsPage, subscribed, participant),
                "Should throw exception if arguments are in other order"
        );
    }
}