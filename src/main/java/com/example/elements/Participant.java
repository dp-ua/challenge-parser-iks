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
}
