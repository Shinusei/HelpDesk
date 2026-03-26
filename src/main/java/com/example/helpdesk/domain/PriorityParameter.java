package com.example.helpdesk.domain;

public enum PriorityParameter {
    IMPORTANCE("Важность"),
    NEWER_UNRESOLVED_TICKETS("Кол-во новых незавершённых заявок"),
    URGENCY("Срочность"),
    IMPACT("Влияние"),
    CATEGORY("Категория (Тематика)"),
    CREATOR_ROLE("Значимость роли заявителя"),
    WAITING_HOURS("Кол-во часов ожидания");
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
