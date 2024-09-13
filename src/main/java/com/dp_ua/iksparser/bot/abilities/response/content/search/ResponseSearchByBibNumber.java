package com.dp_ua.iksparser.bot.abilities.response.content.search;

import com.dp_ua.iksparser.bot.abilities.infoview.CompetitionView;
import com.dp_ua.iksparser.bot.abilities.infoview.SearchView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.response.ResponseTypeMarker;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Optional;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.SEARCH_BY_BIB_NUMBER;

@Component
@Scope("prototype")
@ResponseTypeMarker(SEARCH_BY_BIB_NUMBER)
public class ResponseSearchByBibNumber implements ResponseContentGenerator {
    private static final int ARGS_SIZE = 1;
    private static final int ARGS_COMPETITION_INDEX = 0;
    @Autowired
    SearchView searchView;
    @Autowired
    CompetitionView competitionView;

    @Override
    public String messageText(Object... args) {
        validateArgs(args);
        CompetitionEntity competition = getCompetition(args);

        return searchView.findByBibNumber(competition);
    }

    @Override
    public InlineKeyboardMarkup keyboard(Object... args) {
        validateArgs(args);
        CompetitionEntity competition = getCompetition(args);

        return competitionView.getBackToCompetitionKeyboard(competition);
    }

    private CompetitionEntity getCompetition(Object[] args) {
        return (CompetitionEntity) getArgumentObject(ARGS_COMPETITION_INDEX, args).orElseThrow();
    }

    private void validateArgs(Object[] args) {
        if (args.length != ARGS_SIZE) {
            throw new IllegalArgumentException("Invalid args size: " + args.length + ", expected: " + ARGS_SIZE);
        }
        Optional<?> argumentObject = getArgumentObject(ARGS_COMPETITION_INDEX, args);
        if (argumentObject.isEmpty() || !(argumentObject.get() instanceof CompetitionEntity)) {
            throw new IllegalArgumentException("Invalid argument type: " + argumentObject.get().getClass().getName() +
                    ", expected: " + CompetitionEntity.class.getName());
        }
    }
}
