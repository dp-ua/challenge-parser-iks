package com.dp_ua.iksparser.bot.performer;

import com.dp_ua.iksparser.bot.abilities.subscribe.SubscribeFacade;
import com.dp_ua.iksparser.bot.event.SubscribeEvent;
import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SubscriberPerformer implements ApplicationListener<SubscribeEvent> {
    @Autowired
    SubscribeFacade facade;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    @Transactional
    public void onApplicationEvent(SubscribeEvent event) {
        Map<ParticipantEntity, List<HeatLineEntity>> participations = event.getParticipations();
        log.info("SubscriberPerformer.onApplicationEvent:{}, participants:{}", event, participations.size());
        participations.forEach((participant, heatLines) -> {
            log.debug("SubscriberPerformer.onApplicationEvent:participant:{}, heatLines:{}", participant, heatLines.size());
            facade.operateParticipantWithHeatlines(participant, heatLines);
        });
    }
}
