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
public class HeatDto {

    private long id;
    private String name;
    private List<HeatLineDto> heatLines;

}
