package com.dp_ua.iksparser.service;

import com.dp_ua.iksparser.dba.element.UpdateStatusEntity;
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
