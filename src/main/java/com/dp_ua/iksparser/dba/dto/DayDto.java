package com.dp_ua.iksparser.dba.dto;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class DayDto {

    private long id;
    private String date;
    private String dayName;
    private String dayNameEn;
    private List<Long> events;

}
