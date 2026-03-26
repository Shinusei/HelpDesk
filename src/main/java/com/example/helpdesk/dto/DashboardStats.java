package com.example.helpdesk.dto;

import lombok.Data;

@Data
public class DashboardStats {
    private long totalTickets;
    private long openTickets;
    private long closedTickets;
    private long overdueTickets;
    private double avgResolutionTimeHours;
}
