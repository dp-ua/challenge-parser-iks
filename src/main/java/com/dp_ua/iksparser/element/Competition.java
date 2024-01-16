package com.dp_ua.iksparser.element;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
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

    public String getUrl() {
        return url;
    }

    public void setBegin(LocalDate begin) {
        this.begin = begin;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setTer(String ter) {
        this.ter = ter;
    }

    public String getTer() {
        return ter;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}
