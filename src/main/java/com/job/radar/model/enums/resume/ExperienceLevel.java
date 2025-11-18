package com.job.radar.model.enums.resume;

public enum ExperienceLevel {
    NO_EXPERIENCE("Без опыта"),
    JUNIOR_1_3("1-3 года"),
    MIDDLE_3_6("3-6 лет"),
    SENIOR_6_PLUS("Более 6 лет"),
    EXPERT_10_PLUS("Более 10 лет");

    private final String displayName;

    ExperienceLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}