package com.dp_ua.iksparser.dba.element;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class UpdateStatusEntity extends DomainElement{
    private String chatId;
    private String editMessageId;
    private String competitionId;
    private String status;
}
