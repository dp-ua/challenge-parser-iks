package com.dp_ua.iksparser.dba.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CompetitionStatus {
    C_CANCELED("Скасовано"),
    C_PLANED("Заплановано"),
    C_NOT_STARTED("Відбудеться"),
    C_IN_PROGRESS("Проводиться"),
    C_FINISHED("Завершено");

    private final String name;

    public static CompetitionStatus getByName(String name) {
        for (CompetitionStatus status : CompetitionStatus.values()) {
            if (status.getName().equals(name)) {
                return status;
            }
        }
        return null;
    }
}
