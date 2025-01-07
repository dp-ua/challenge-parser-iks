package com.dp_ua.iksparser.dba.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum CompetitionStatus {
    C_CANCELED("Скасовано"),
    C_PLANED("Заплановано"),
    C_NOT_STARTED("Відбудеться"),
    C_IN_PROGRESS("Проводиться"),
    C_FINISHED("Завершено");

    private final String name;

    public static Optional<CompetitionStatus> getByName(String name) {
        for (CompetitionStatus status : CompetitionStatus.values()) {
            if (status.getName().equals(name)) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}
