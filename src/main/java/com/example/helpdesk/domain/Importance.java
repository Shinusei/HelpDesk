package com.example.helpdesk.domain;

public enum Importance {
    LOW("Низкая"),
    MEDIUM("Средняя"),
    HIGH("Высокая");

    private final String displayName;

    Importance(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
