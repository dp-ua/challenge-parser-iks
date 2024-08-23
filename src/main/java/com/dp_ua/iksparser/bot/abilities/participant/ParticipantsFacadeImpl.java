package com.dp_ua.iksparser.bot.abilities.participant;

import com.dp_ua.iksparser.bot.abilities.FacadeMethods;
import com.dp_ua.iksparser.bot.abilities.StateService;
import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContent;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandParticipants;
import com.dp_ua.iksparser.bot.command.impl.participants.CommandShowFindAllParticipants;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Optional;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.PARTICIPANTS_VIEW_MAIN;
import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.SHOW_ALL_PARTICIPANTS;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Component
@Slf4j
public class ParticipantsFacadeImpl extends FacadeMethods implements ParticipantFacade {
    @Value("${view.participants.pageSize}")
    private int pageSize;
    @Autowired
    private ParticipantService participantService;
    @Autowired
    SubscribeFacade subscribeFacade;
    @Autowired
    ParticipantView participantView;
    @Autowired
    StateService stateService;

    @Override
    public void subscribe(String chatId, long commandArgument, Integer editMessageId) {
        log.info("SUBSCRIBE chatId: {}, commandArgument: {}", chatId, commandArgument);
        Optional<ParticipantEntity> optionalParticipant = participantService.findById(commandArgument);
        optionalParticipant.ifPresent(participant -> {
            subscribeFacade.subscribe(chatId, participant);
            subscribeFacade.inform(chatId, participant, editMessageId);
        });
    }

    @Override
    public void unsubscribe(String chatId, long commandArgument, Integer editMessageId) {
        log.info("UNSUBSCRIBE chatId: {}, commandArgument: {}", chatId, commandArgument);
        Optional<ParticipantEntity> optionalParticipant = participantService.findById(commandArgument);
        optionalParticipant.ifPresent(participant -> {
            subscribeFacade.unsubscribe(chatId, participant);
            subscribeFacade.inform(chatId, participant, editMessageId);
        });
    }

    @Override
    public String getInfoAboutParticipants() {
        long count = participantService.getCount();
        return participantView.participantsInfo(count);
    }

    @Override
    public void showParticipants(String chatId, long commandArgument, Integer editMessageId) {
        log.info("SHOW PARTICIPANTS chatId: {}, commandArgument: {}", chatId, commandArgument);

        ResponseContent content = contentFactory.getContentForResponse(PARTICIPANTS_VIEW_MAIN);
        validate(content, "ResponseContent for PARTICIPANTS_VIEW_MAIN not found");

        SendMessageEvent event = SERVICE.getSendMessageEvent(chatId, editMessageId, content);
        publisher.publishEvent(event);
    }

    @Override
    public void showFindAllParticipants(String chatId, String commandArgument, Integer editMessageId) {
        log.info("SHOW FIND ALL PARTICIPANTS chatId: {}, commandArgument: {}", chatId, commandArgument);

        ResponseContent content = contentFactory.getContentForResponse(SHOW_ALL_PARTICIPANTS);
        validate(content, "ResponseContent for SHOW_ALL_PARTICIPANTS not found");

        String pageString = jSonReader.getVal(commandArgument, "page");
        validate(pageString, "Page not found");
        int page = normalizeArgument(Integer.parseInt(pageString));

        String search = jSonReader.getVal(commandArgument, "search");
        validate(search, "Search not found");

        Page<ParticipantEntity> participants = participantService.findAllBySurnameAndNameParts(List.of(search), page, pageSize);

        String text = content.getMessageText(participants, search);
        InlineKeyboardMarkup keyboard = content.getKeyboard(participants, search);

        List<InlineKeyboardButton> backButton = SERVICE.getBackButton("/" + CommandParticipants.command);
        keyboard.getKeyboard().add(backButton);

        setStateShowFindAll(chatId, page);
        SendMessageEvent event = SERVICE.getSendMessageEvent(chatId, text, keyboard, editMessageId);
        publisher.publishEvent(event);
    }

    private void setStateShowFindAll(String chatId, int page) {
        stateService.setState(chatId, CommandShowFindAllParticipants.getStateText(page));
    }

    private static int normalizeArgument(long commandArgument) {
        return commandArgument < 0 ? 0 : (int) commandArgument;
    }
}
