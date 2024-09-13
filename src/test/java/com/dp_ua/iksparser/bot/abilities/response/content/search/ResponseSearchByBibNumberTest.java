package com.dp_ua.iksparser.bot.abilities.response.content.search;

import com.dp_ua.iksparser.bot.abilities.infoview.CompetitionView;
import com.dp_ua.iksparser.bot.abilities.infoview.SearchView;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = ResponseSearchByBibNumber.class)
class ResponseSearchByBibNumberTest {
    @MockBean
    SearchView searchView;
    @MockBean
    CompetitionView competitionView;
    @Autowired
    ResponseSearchByBibNumber generator;

    @Test
    public void shouldNotThrowException() {
        CompetitionEntity competition = new CompetitionEntity();

        assertDoesNotThrow(() -> generator.getContainer(competition));
    }
}