package com.dp_ua.iksparser.dba.element.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
