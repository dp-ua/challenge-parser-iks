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
public class HeatDto {
    private String name;
    private List<Long> heatLines;
}
