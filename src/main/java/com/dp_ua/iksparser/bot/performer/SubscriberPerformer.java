package com.dp_ua.iksparser.bot.performer;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.dp_ua.iksparser.bot.abilities.notification.NotificationQueueService;
import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.event.SubscribeEvent;
import com.dp_ua.iksparser.configuration.NotificationConfigProperties;
import com.dp_ua.iksparser.dba.entity.HeatLineEntity;
import com.dp_ua.iksparser.dba.entity.ParticipantEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriberPerformer implements ApplicationListener<SubscribeEvent> {

    private final SubscribeFacade facade;
    private final NotificationQueueService notificationQueueService;
    private final NotificationConfigProperties notificationConfig;

    @Override
    @Transactional
    public void onApplicationEvent(SubscribeEvent event) {
        Map<ParticipantEntity, List<HeatLineEntity>> participations = event.getParticipations();
        log.info("SubscriberPerformer.onApplicationEvent:{}, participants:{}", event, participations.size());

        participations.forEach((participant, heatLines) -> {
            log.debug("SubscriberPerformer.onApplicationEvent:participant:{}, heatLines:{}", participant, heatLines.size());

            // Legacy notification - send immediately if enabled
            if (notificationConfig.isLegacyEnabled()) {
                facade.operateParticipantWithHeatlines(participant, heatLines);
            }

            // Aggregated notification - save to queue if enabled
            if (notificationConfig.isAggregatedEnabled()) {
                notificationQueueService.saveNotificationsToQueue(participant, heatLines);
            }
        });
    }

}
