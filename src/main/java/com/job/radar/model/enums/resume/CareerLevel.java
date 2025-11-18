package com.job.radar.model.enums.resume;

public enum CareerLevel {
    INTERN("Стажер"),
    JUNIOR("Младший специалист"),
    MIDDLE("Специалист"),
    SENIOR("Старший специалист"),
    LEAD("Ведущий специалист"),
    MANAGER("Менеджер"),
    DIRECTOR("Директор");

    private final String displayName;

    CareerLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
