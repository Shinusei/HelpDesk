package com.example.helpdesk.domain;

public enum PriorityParameter {
    IMPORTANCE("Важность");
    // Здесь можно добавить другие предопределенные параметры, например:
    // URGENCY("Срочность"),
    // IMPACT("Влияние"),
    // DEPARTMENT("Отдел");

    private final String displayName;

    PriorityParameter(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
