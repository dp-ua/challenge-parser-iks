package com.dp_ua.iksparser.bot.event;

import com.dp_ua.iksparser.dba.entity.UpdateStatusEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UpdateCompetitionEvent extends ApplicationEvent {
    private final UpdateStatusEntity message;

    public UpdateCompetitionEvent(Object source, UpdateStatusEntity message) {
        super(source);
        this.message = message;
    }
}
