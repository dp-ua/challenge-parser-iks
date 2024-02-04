package com.dp_ua.iksparser.dba.element;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class SubscriberEntity extends DomainElement {
    private String chatId;
    @OneToOne
    @JoinColumn(name = "participant_id")
    private ParticipantEntity participant;

    @Override
    public String toString() {
        return "SubscriberEntity{" +
                "chatId='" + chatId + '\'' +
                ", participant=" + participant.getSurname() + " " + participant.getName() +
                '}';
    }
}
