package com.example.helpdesk.controller.admin;

import com.example.helpdesk.dto.DashboardStats;
import com.example.helpdesk.service.TicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasAnyAuthority('ROLE_IT_SUPPORT', 'ROLE_ADMIN')")
public class DashboardRestController {

    private final TicketService ticketService;

    public DashboardRestController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/stats")
    public DashboardStats getStats() {
        return ticketService.getDashboardStats();
    }
}
