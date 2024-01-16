package com.dp_ua.iksparser.element;

import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Competition {
    private String url;
    private LocalDate begin;
    private LocalDate end;
    private String name;
    private String country;
    private String ter;
    private String city;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBegin(LocalDate begin) {
        this.begin = begin;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setTer(String ter) {
        this.ter = ter;
    }

    public void setCity(String city) {
        this.city = city;
    }

}
