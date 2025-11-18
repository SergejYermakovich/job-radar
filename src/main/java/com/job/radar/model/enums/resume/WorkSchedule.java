package com.job.radar.model.enums.resume;

public enum WorkSchedule {
    FULL_TIME("Полный день"),
    PART_TIME("Неполный день"),
    FLEXIBLE("Гибкий график"),
    SHIFT_WORK("Сменный график"),
    REMOTE("Удаленная работа");

    private final String displayName;

    WorkSchedule(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
