package com.dp_ua.iksparser.dba.element;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CompetitionStatus {
    CANCELED("Скасовано"),
    PLANED("Заплановано"),
    NOT_STARTED("Відбудеться"),
    IN_PROGRESS("Проводиться"),
    FINISHED("Завершено");

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
