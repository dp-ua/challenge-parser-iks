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
public class HeatLineDto {

    private long id;
    private String lane;
    private String bib;
    private ParticipantDto participant;
    private List<Long> coaches;

}
