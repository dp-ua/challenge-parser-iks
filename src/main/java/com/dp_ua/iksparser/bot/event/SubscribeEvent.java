package com.dp_ua.iksparser.bot.event;

import com.dp_ua.iksparser.dba.element.HeatLineEntity;
import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Map;

@Getter
public class SubscribeEvent extends ApplicationEvent {
    private final Map<ParticipantEntity, List<HeatLineEntity>> participations;

    public SubscribeEvent(Object source, Map<ParticipantEntity, List<HeatLineEntity>> participations) {
        super(source);
        this.participations = participations;
    }
}
