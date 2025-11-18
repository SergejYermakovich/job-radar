package com.job.radar.model.enums.resume;

public enum EducationLevel {
    SECONDARY("Среднее"),
    COLLEGE("Среднее специальное"),
    BACHELOR("Бакалавр"),
    MASTER("Магистр"),
    PHD("Кандидат/Доктор наук");

    private final String displayName;

    EducationLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
