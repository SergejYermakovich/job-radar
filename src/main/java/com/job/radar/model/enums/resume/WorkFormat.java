package com.job.radar.model.enums.resume;

public enum WorkFormat {
    OFFICE("Только офис"),
    REMOTE("Только удаленно"),
    HYBRID("Гибридный формат"),
    FLEXIBLE("Гибкий график"),
    ANY("Любой формат");

    private final String displayName;

    WorkFormat(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}