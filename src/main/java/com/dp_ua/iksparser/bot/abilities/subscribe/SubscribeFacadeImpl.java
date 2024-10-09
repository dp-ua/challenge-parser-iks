package com.dp_ua.iksparser.bot.abilities.subscribe;

import com.dp_ua.iksparser.bot.abilities.infoview.SubscriptionView;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContainer;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentFactory;
import com.dp_ua.iksparser.bot.abilities.response.ResponseContentGenerator;
import com.dp_ua.iksparser.bot.event.SendMessageEvent;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;
import com.dp_ua.iksparser.dba.entity.SubscriberEntity;
import com.dp_ua.iksparser.dba.service.CompetitionService;
import com.dp_ua.iksparser.dba.service.SubscriberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.NEW_EVENT_INFORM;
import static com.dp_ua.iksparser.bot.abilities.response.ResponseType.SUBSCRIPTIONS_LIST;
import static com.dp_ua.iksparser.service.MessageCreator.SERVICE;

@Component
@Slf4j
public class SubscribeFacadeImpl implements SubscribeFacade {
    @Value("${view.participants.pageSize}")
    private int pageSize;
    @Autowired
    SubscriberService subscriberService;
    @Autowired
    ApplicationEventPublisher publisher;
    @Autowired
    ResponseContentFactory responseContentFactory;

    @Autowired
    SubscriptionView view;
    @Autowired
    CompetitionService competitionService;

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
    public void operateParticipantWithHeatlines(ParticipantEntity participant, List<HeatLineEntity> heatLines) {
        ResponseContentGenerator generator = responseContentFactory.getContentForResponse(NEW_EVENT_INFORM);

        subscriberService.findAllByParticipant(participant).forEach(subscriber ->
                competitionService.getCompetitionByHeatLine(heatLines.get(0)).ifPresent(competition -> {
                    log.info("Informing subscriber: {} about participant: {} with heatlines: {}", subscriber, participant, heatLines.size());
                    ResponseContainer content = generator.getContainer(participant, competition, heatLines);

                    SendMessageEvent sendMessageEvent = SERVICE.getSendMessageEvent(subscriber.getChatId(), null, content);
                    publisher.publishEvent(sendMessageEvent);
                }));
    }

    @Override
    public void showSubscriptions(String chatId, long commandArgument, Integer editMessageId) {
        List<SubscriberEntity> subscriptions = getSubscriptions(chatId);
        SendMessageEvent sendMessageEvent = prepareSendSubscribersEvent(chatId, subscriptions, editMessageId);
        publisher.publishEvent(sendMessageEvent);
    }

    @Override
    public void showSubscriptionsList(String chatId, int page, Integer editMessageId) {
        Page<ParticipantEntity> subscriptions = getSubscriptions(chatId, page);

        ResponseContentGenerator generator = responseContentFactory.getContentForResponse(SUBSCRIPTIONS_LIST);
        ResponseContainer content = generator.getContainer(subscriptions);

        SendMessageEvent sendMessageEvent = SERVICE.getSendMessageEvent(chatId, editMessageId, content);
        publisher.publishEvent(sendMessageEvent);
    }

    private Page<ParticipantEntity> getSubscriptions(String chatId, int page) {
        return subscriberService.getSubscriptions(chatId, page, pageSize);
    }

    @Override
    public String getInfoAboutSubscribes(String chatId) {
        int count = subscriberService.subscribersCount(chatId);
        return view.subscriptionsDetails(count);
    }

    private List<SubscriberEntity> getSubscriptions(String chatId) {
        return subscriberService.findAllByChatId(chatId);
    }

    private SendMessageEvent prepareSendSubscribersEvent(String chatId, List<SubscriberEntity> subscribers, Integer editMessageId) {
        String text = view.subscriptions(subscribers);
        InlineKeyboardMarkup keyboard = view.getSubscriptionsKeyboard();
        return SERVICE.getSendMessageEvent(chatId, text, keyboard, editMessageId);
    }
}
