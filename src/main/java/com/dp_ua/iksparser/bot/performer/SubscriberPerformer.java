package com.dp_ua.iksparser.bot.performer;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueService;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.event.SubscribeEvent;
import com.dp_ua.iksparser.configuration.NotificationConfigProperties;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriberPerformer implements ApplicationListener<SubscribeEvent> {

    private final SubscribeFacade subscribeFacade;
    private final NotificationQueueService notificationQueueService;
    private final NotificationConfigProperties notificationConfig;

    @Override
    @Transactional
    public void onApplicationEvent(SubscribeEvent event) {
        var participations = event.getParticipations();
        log.info("SubscriberPerformer.onApplicationEvent:{}, participants:{}", event, participations.size());

        if (notificationConfig.isLegacyEnabled()) {
            participations.forEach(subscribeFacade::operateParticipantWithHeatlines);
        }

        if (notificationConfig.isAggregatedEnabled()) {
            participations.forEach(notificationQueueService::saveNotificationsToQueue);
        }
    }

}
