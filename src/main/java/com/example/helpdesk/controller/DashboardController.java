package com.example.helpdesk.controller;

import com.example.helpdesk.service.TicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
@PreAuthorize("hasAnyRole('ROLE_IT_SUPPORT', 'ROLE_ADMIN')")
public class DashboardController {

    private final TicketService ticketService;

    public DashboardController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public String viewDashboard(Model model) {
        model.addAttribute("stats", ticketService.getDashboardStats());
        return "admin/dashboard";
    }
}
