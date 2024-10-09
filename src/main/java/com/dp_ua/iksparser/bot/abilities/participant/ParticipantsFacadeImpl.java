package com.dp_ua.iksparser.bot.abilities.participant;

import com.dp_ua.iksparser.bot.abilities.FacadeMethods;
import com.dp_ua.iksparser.bot.abilities.StateService;
import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContainer;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandParticipants;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandShowFindAllParticipants;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.dba.entity.CompetitionEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.*;
import static com.dp_ua.iksparser.bot.command.CommandArgumentName.*;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Component
@Slf4j
public class ParticipantsFacadeImpl extends FacadeMethods implements ParticipantFacade {
    @Value("${view.participants.pageSize}")
    private int PARTICIPANTS_PAGE_SIZE;
    @Value("${view.competitions.detailed.pageSize}")
    private int COMPETITION_PAGE_SIZE;
    @Autowired
    private ParticipantService participantService;
    @Autowired
    SubscribeFacade subscribeFacade;
    @Autowired
    ParticipantView participantView;
    @Autowired
    StateService stateService;
    @Autowired
    CompetitionService competitionService;

    @Override
    public String getInfoAboutParticipants() {
        long count = participantService.getCount();
        return participantView.participantsInfo(count);
    }

    @Override
    public void showParticipants(String chatId, long commandArgument, Integer editMessageId) {
        log.info("SHOW PARTICIPANTS chatId: {}, commandArgument: {}", chatId, commandArgument);

        ResponseContentGenerator contentGenerator = contentFactory.getContentForResponse(PARTICIPANTS_VIEW_MAIN);
        validate(contentGenerator, "ResponseContent for PARTICIPANTS_VIEW_MAIN not found");
        ResponseContainer content = contentGenerator.getContainer();

        setStateShowFindAll(chatId);
        SendMessageEvent event = SERVICE.getSendMessageEvent(chatId, editMessageId, content);
        publisher.publishEvent(event);
    }

    @Override
    public void showFindAllParticipants(String chatId, String jsonArguments, Integer editMessageId) {
        log.info("SHOW FIND ALL PARTICIPANTS chatId: {}, commandArgument: {}", chatId, jsonArguments);

        ResponseContentGenerator content = contentFactory.getContentForResponse(SHOW_ALL_PARTICIPANTS);
        validate(content, "ResponseContent for SHOW_ALL_PARTICIPANTS not found");

        String pageString = jSonReader.getVal(jsonArguments, PAGE.getValue());
        validate(pageString, "Page not found");
        int page = normalizeArgument(Integer.parseInt(pageString));

        String search = jSonReader.getVal(jsonArguments, SEARCH.getValue());
        validate(search, "Search not found");

        Page<ParticipantEntity> participants = participantService.findAllBySurnameAndNameParts(List.of(search), page, PARTICIPANTS_PAGE_SIZE);
        ResponseContainer container = content.getContainer(participants, search);

        List<InlineKeyboardButton> backButton = SERVICE.getBackButton(CommandParticipants.getCallbackCommand());
        container.getKeyboard().getKeyboard().add(backButton);

        setStateShowFindAll(chatId);
        SendMessageEvent event = SERVICE.getSendMessageEvent(chatId, editMessageId, container);

        publisher.publishEvent(event);
    }

    @Override
    public void showParticipantDetails(String chatId, String jsonArguments, Integer editMessageId) {
        log.info("SHOW PARTICIPANT DETAILS chatId: {}, commandArgument: {}", chatId, jsonArguments);

        long id = Long.parseLong(jSonReader.getVal(jsonArguments, PARTICIPANT_ID.getValue()));
        int page = Integer.parseInt(jSonReader.getVal(jsonArguments, PAGE.getValue()));

        participantService.findById(id).ifPresent(participant -> {
            boolean subscribed = subscribeFacade.isSubscribed(chatId, participant);
            Page<CompetitionEntity> competitions = competitionService.findCompetitionsByParticipant(participant, page, COMPETITION_PAGE_SIZE);

            ResponseContentGenerator content = contentFactory.getContentForResponse(PARTICIPANT_DETAILS);
            validate(content, "ResponseContent for PARTICIPANTS_VIEW_MAIN not found");
            ResponseContainer container = content.getContainer(participant, competitions, subscribed);

            SendMessageEvent event = SERVICE.getSendMessageEvent(chatId, editMessageId, container);
            publisher.publishEvent(event);
        });
    }

    private void setStateShowFindAll(String chatId) {
        stateService.setState(chatId, CommandShowFindAllParticipants.getStateText(0));
    }

    private static int normalizeArgument(long commandArgument) {
        return commandArgument < 0 ? 0 : (int) commandArgument;
    }
}
