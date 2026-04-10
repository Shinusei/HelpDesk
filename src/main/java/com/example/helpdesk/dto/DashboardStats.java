package com.example.helpdesk.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardStats {
    private long totalTickets;
    private long openTickets;
    private long closedTickets;
    private long overdueTickets;
    private double avgResolutionTimeHours;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> categoryDistribution;
}
