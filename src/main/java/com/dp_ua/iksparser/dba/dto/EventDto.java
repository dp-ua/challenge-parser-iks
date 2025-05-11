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
public class EventDto {

    private Long id;
    private String time;
    private String eventName;
    private String category;
    private String round;
    private String startListUrl;
    private String resultUrl;
    private List<HeatDto> heats;

}
