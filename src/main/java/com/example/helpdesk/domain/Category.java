package com.example.helpdesk.domain;

public enum Category {
    HARDWARE("Оборудование"),
    SOFTWARE("ПО"),
    NETWORK("Сеть"),
    ACCESS("Доступы"),
    OTHER("Другое");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
