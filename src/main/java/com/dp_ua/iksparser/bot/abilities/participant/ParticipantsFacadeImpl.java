package com.dp_ua.iksparser.bot.abilities.participant;

import com.dp_ua.iksparser.bot.Icon;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.dp_ua.iksparser.service.MessageCreator.BOLD;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;

@Component
@Slf4j
public class ParticipantsFacadeImpl implements ParticipantFacade {
    @Autowired
    private ParticipantService participantService;
    @Autowired
    SubscribeFacade subscribeFacade;

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
        Iterable<ParticipantEntity> participants = participantService.findAll();
        // todo move to ParticipantsView

        return Icon.ATHLETE +
                "Всього атлетів в базі: " +
                BOLD +
                participants.spliterator().getExactSizeIfKnown() +
                BOLD +
                END_LINE;
    }
}
