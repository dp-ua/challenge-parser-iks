package com.dp_ua.iksparser.dba.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
public class UpdateStatusEntity extends DomainElement {
    private String chatId;
    private Integer editMessageId;
    private long competitionId;
    private String status;
    private String reason;
}
