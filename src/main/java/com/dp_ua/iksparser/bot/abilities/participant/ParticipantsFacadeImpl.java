package com.dp_ua.iksparser.bot.abilities.participant;

import com.dp_ua.iksparser.bot.abilities.FacadeMethods;
import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContent;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.PARTICIPANTS_VIEW_MAIN;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Component
@Slf4j
public class ParticipantsFacadeImpl extends FacadeMethods implements ParticipantFacade {
    @Autowired
    private ParticipantService participantService;
    @Autowired
    SubscribeFacade subscribeFacade;
    @Autowired
    ParticipantView participantView;

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
}
