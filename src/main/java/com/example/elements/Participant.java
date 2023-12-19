package com.example.elements;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Participant {
    private String lane;
    private String bib;
    private String surname;

    private String name;
    private String born;
    private String url;

    public Participant(String lane, String bib, String surname, String name, String born, String url) {
        this.lane = lane;
        this.bib = bib;
        this.surname = surname;
        this.name = name;
        this.born = born;
        this.url = url;
    }
}
