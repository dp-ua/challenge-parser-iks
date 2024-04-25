package com.dp_ua.iksparser.dba.element.dto;

import com.dp_ua.iksparser.dba.element.ParticipantEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class HeatLineDto {
    private String lane;
    private String bib;
    private ParticipantEntity participant;
    private List<Long> coaches;
}
