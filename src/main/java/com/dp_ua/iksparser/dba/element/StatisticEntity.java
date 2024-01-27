package com.dp_ua.iksparser.dba.element;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
public class StatisticEntity extends DomainElement {
    private String chatId;
    private String text;
}
