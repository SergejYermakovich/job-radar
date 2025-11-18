package com.job.radar.model.enums.resume;

public enum EmploymentType {
    FULL_EMPLOYMENT("Полная занятость"),
    PART_EMPLOYMENT("Частичная занятость"),
    PROJECT_WORK("Проектная работа"),
    INTERNSHIP("Стажировка"),
    VOLUNTEER("Волонтерство");

    private final String displayName;

    EmploymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
