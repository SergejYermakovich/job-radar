package com.job.radar.model.enums.resume;

public enum BusinessTripReadiness {
    NEVER("Не готов к командировкам"),
    RARE("Редкие командировки"),
    READY("Готов к командировкам"),
    OFTEN("Частые командировки");

    private final String displayName;

    BusinessTripReadiness(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}