package com.dp_ua.iksparser.dba.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class CompetitionDto {
    private long id;
    private List<Long> days;
    private String name;
    private String status;
    private String beginDate;
    private String endDate;
    private String country;
    private String city;
    private String url;
}
