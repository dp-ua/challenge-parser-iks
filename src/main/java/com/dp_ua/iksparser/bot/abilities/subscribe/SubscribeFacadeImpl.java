package com.dp_ua.iksparser.bot.abilities.subscribe;

import com.dp_ua.iksparser.bot.Icon;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static com.dp_ua.iksparser.service.MessageCreator.*;

@Component
@Slf4j
public class SubscribeFacadeImpl implements SubscribeFacade {
    @Autowired
    SubscriberService subscriberService;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    public boolean isSubscribed(String chatId, ParticipantEntity participant) {
        return subscriberService.isSubscribed(chatId, participant);
    }

    @Override
    public void subscribe(String chatId, ParticipantEntity participant) {
        log.info("Subscribing chatId: {}, participant: {}", chatId, participant);
        subscriberService.subscribe(chatId, participant);
    }

    @Override
    public void unsubscribe(String chatId) {
        log.info("Unsubscribing chatId: {}", chatId);
        subscriberService.findAllByChatId(chatId)
                .forEach(subscriber ->
                        unsubscribe(subscriber.getChatId(), subscriber.getParticipant()));
    }

    @Override
    public void unsubscribe(String chatId, ParticipantEntity participant) {
        log.info("Unsubscribing chatId: {}, participant: {}", chatId, participant);
        subscriberService.unsubscribe(chatId, participant);
    }

    @Override
    public void inform(String chatId, ParticipantEntity participant, Integer editMessageId) {
        boolean subscribed = isSubscribed(chatId, participant);

        String text = SubscriptionView.subscriptionText(participant, subscribed);
        InlineKeyboardMarkup button = SubscriptionView.button(participant, subscribed);
        publisher.publishEvent(
                SERVICE.getSendMessageEvent(chatId, text, button, editMessageId)
        );
    }

    @Override
    public void operateParticipantWithHeatlines(ParticipantEntity participant, List<HeatLineEntity> heatLines) {
        subscriberService.findAllByParticipant(participant).forEach(subscriber -> {
            log.debug("Informing subscriber: {} about participant: {} with heatlines: {}", subscriber, participant, heatLines.size());
            sendMessageToSubscriber(subscriber, participant, heatLines);
        });
    }

    @Override
    public void showSubscriptions(String chatId, long commandArgument, Integer editMessageId) {
        List<SubscriberEntity> subscriptions = getSubscriptions(chatId);
        SendMessageEvent sendMessageEvent = prepareSendSubscribersEvent(chatId, subscriptions, editMessageId);
        publisher.publishEvent(sendMessageEvent);
    }

    @Override
    public void showSubscriptionsList(String chatId, long commandArgument, Integer editMessageId) {
        List<SubscriberEntity> subscriptions = getSubscriptions(chatId);
        // TODO:
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getInfoAboutSubscribes(String chatId) {
        List<SubscriberEntity> subscriptions = subscriberService.findAllByChatId(chatId);
        // todo move to SubscriptionView

        return Icon.SUBSCRIBE +
                "Ви підписані на: " +
                BOLD +
                subscriptions.size() +
                BOLD +
                " атл." +
                END_LINE;
    }

    private List<SubscriberEntity> getSubscriptions(String chatId) {
        return subscriberService.findAllByChatId(chatId);
    }

    private SendMessageEvent prepareSendSubscribersEvent(String chatId, List<SubscriberEntity> subscribers, Integer editMessageId) {
        String text = SubscriptionView.subscriptions(subscribers);
        InlineKeyboardMarkup keyboard = SubscriptionView.getSubscriptionsKeyboard(subscribers);
        return SERVICE.getSendMessageEvent(chatId, text, keyboard, editMessageId);
    }

    private void sendMessageToSubscriber(SubscriberEntity subscriber, ParticipantEntity participant, List<HeatLineEntity> heatLines) {
        CompetitionEntity competition = heatLines.get(0).getHeat().getEvent().getDay().getCompetition();
        SendMessageEvent sendMessageEvent = prepareSendEvent(subscriber, participant, heatLines, competition);
        publisher.publishEvent(sendMessageEvent);
    }

    private SendMessageEvent prepareSendEvent(SubscriberEntity subscriber, ParticipantEntity participant, List<HeatLineEntity> heatLines, CompetitionEntity competition) {
        String chatId = subscriber.getChatId();
        String text = SubscriptionView.info(participant, heatLines, competition);
        InlineKeyboardMarkup keyboard = SubscriptionView.button(participant, true);
        return SERVICE.getSendMessageEvent(chatId, text, keyboard, null);
    }
}
