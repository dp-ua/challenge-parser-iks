package com.dp_ua.iksparser.dba.element;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class StatisticEntity extends DomainElement {
    private String chatId;
    private String name;
    private String text;

    @Override
    public String toString() {
        return "StatisticEntity{" +
                "chatId='" + chatId + '\'' +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
