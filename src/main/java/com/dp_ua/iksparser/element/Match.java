package com.dp_ua.iksparser.element;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter@Setter
@EqualsAndHashCode
public class Match {
    private Day day;
    private Event event;
    private Heat heat;
    private Participant participant;
}