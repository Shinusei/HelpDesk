package com.example.helpdesk.domain;

public enum Urgency {
    LOW("Низкая"),
    MEDIUM("Средняя"),
    HIGH("Высокая"),
    CRITICAL("Критическая");

    private final String displayName;

    Urgency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
