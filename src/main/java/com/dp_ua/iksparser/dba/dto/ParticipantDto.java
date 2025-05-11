package com.dp_ua.iksparser.dba.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class ParticipantDto {

    private long id;
    private String surname;
    private String name;
    private String team;
    private String region;
    private String born;
    private String url;

}
