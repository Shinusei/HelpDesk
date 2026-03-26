package com.example.helpdesk.domain;

public enum Impact {
    USER("Один пользователь"),
    DEPARTMENT("Отдел"),
    COMPANY("Вся компания");

    private final String displayName;

    Impact(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
