package com.dp_ua.iksparser.bot.abilities.subscribe;

import com.dp_ua.iksparser.bot.abilities.infoview.SubscriptionView;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.dba.element.CompetitionEntity;
import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import com.dp_ua.iksparser.dba.element.SubscriberEntity;
import com.dp_ua.iksparser.dba.service.SubscriberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Component
@Slf4j
public class SubscribeFacadeImpl implements SubscribeFacade {
    @Autowired
    SubscriberService subscriberService;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    public boolean isSubscribed(String chatId, Long id) {
        return subscriberService.isSubscribed(chatId, id);
    }

    @Override
    public void operateParticipantWithHeatlines(ParticipantEntity participant, List<HeatLineEntity> heatLines) {
        subscriberService.findAllByParticipant(participant).forEach(subscriber -> {
            log.debug("Informing subscriber: {} about participant: {} with heatlines: {}", subscriber, participant, heatLines.size());
            sendMessageToSubscriber(subscriber, participant, heatLines);
        });
    }

    private void sendMessageToSubscriber(SubscriberEntity subscriber, ParticipantEntity participant, List<HeatLineEntity> heatLines) {
        CompetitionEntity competition = heatLines.get(0).getHeat().getEvent().getDay().getCompetition();
        SendMessageEvent sendMessageEvent = prepareSendEvent(subscriber, participant, heatLines, competition);
        publisher.publishEvent(sendMessageEvent);
    }

    private SendMessageEvent prepareSendEvent(SubscriberEntity subscriber, ParticipantEntity participant, List<HeatLineEntity> heatLines, CompetitionEntity competition) {
        SendMessage sendMessage = SERVICE.getSendMessage(
                subscriber.getChatId(),
                SubscriptionView.info(participant, heatLines, competition),
                null, // todo UNSUBSCRIBE_KEYBOARD
                true);
        sendMessage.disableWebPagePreview();
        SendMessageEvent sendMessageEvent = new SendMessageEvent(this, sendMessage, SendMessageEvent.MsgType.SEND_MESSAGE);
        return sendMessageEvent;
    }


}
