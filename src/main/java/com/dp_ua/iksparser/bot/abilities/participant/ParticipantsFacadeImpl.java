package com.dp_ua.iksparser.bot.abilities.participant;

import com.dp_ua.iksparser.bot.abilities.infoview.ParticipantView;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.service.ParticipantService;
import com.dp_ua.iksparser.dba.service.SubscriberService;
import com.dp_ua.iksparser.service.MessageCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Optional;

import static com.dp_ua.iksparser.bot.Icon.SUBSCRIBE;
import static com.dp_ua.iksparser.bot.Icon.UNSUBSCRIBE;
import static com.dp_ua.iksparser.service.MessageCreator.END_LINE;

@Component
@Slf4j
public class ParticipantsFacadeImpl implements ParticipantFacade {
    @Autowired
    private ParticipantService participantService;
    @Autowired
    SubscriberService subscriberService;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    public void subscribe(String chatId, long commandArgument, Integer editMessageId) {
        log.info("SUBSCRIBE chatId: {}, commandArgument: {}", chatId, commandArgument);
        Optional<ParticipantEntity> optionalParticipant = participantService.findById(commandArgument);
        optionalParticipant.ifPresent(participant -> {
            if (subscriberService.isSubscribed(chatId, participant.getId())) {
                log.info("chatId: {} already subscribed to participant: {}", chatId, participant.getId());
            } else {
                subscriberService.subscribe(chatId, participant);
                log.info("chatId: {} subscribed to participant: {}", chatId, participant.getId());
            }
        });
        optionalParticipant.ifPresent(participant -> {
            String text = SUBSCRIBE +
                    " Ви підписались на спортсмена: " +
                    END_LINE +
                    ParticipantView.info(participant);
            SendMessageEvent sendMessageEvent = prepareSendEvent(chatId, editMessageId, text, null);
            // todo UNSUBSCRIBE_KEYBOARD
            publisher.publishEvent(sendMessageEvent);
        });
    }

    @Override
    public void unsubscribe(String chatId, long commandArgument, Integer editMessageId) {
        log.info("UNSUBSCRIBE chatId: {}, commandArgument: {}", chatId, commandArgument);
        Optional<ParticipantEntity> optionalParticipant = participantService.findById(commandArgument);
        optionalParticipant.ifPresent(participant -> {
            if (subscriberService.isSubscribed(chatId, participant.getId())) {
                subscriberService.unsubscribe(chatId, participant.getId());
                log.info("chatId: {} unsubscribed from participant: {}", chatId, participant.getId());
            } else {
                log.info("chatId: {} not subscribed to participant: {}", chatId, participant.getId());
            }
        });
        optionalParticipant.ifPresent(participant -> {
            String text = UNSUBSCRIBE +
                    " Ви відписались від спортсмена: " +
                    END_LINE +
                    ParticipantView.info(participant);
            SendMessageEvent sendMessageEvent = prepareSendEvent(chatId, editMessageId, text, null);
            // todo SUBSCRIBE_KEYBOARD
            publisher.publishEvent(sendMessageEvent);
        });
    }


    private SendMessageEvent prepareSendEvent(String chatId, Integer editMessageId, String text, InlineKeyboardMarkup keyboard) {

        EditMessageText editMessageText = MessageCreator.SERVICE.getEditMessageText(
                chatId,
                editMessageId,
                text,
                keyboard,
                true);
        return new SendMessageEvent(this, editMessageText, SendMessageEvent.MsgType.EDIT_MESSAGE);
    }
}
