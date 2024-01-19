package com.dp_ua.iksparser.element;

import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class ParticipantEntity extends DomainElement {
    private String lane;
    private String bib;
    private String surname;

    private String name;
    private String born;
    private String url;
}
