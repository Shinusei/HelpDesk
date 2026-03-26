package com.example.helpdesk.controller;

import com.example.helpdesk.domain.Importance; // Импортируем Importance
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.User;
import com.example.helpdesk.repository.UserRepository;
import com.example.helpdesk.service.TicketService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    public TicketController(TicketService ticketService, UserRepository userRepository) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listTickets(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        List<Ticket> tickets;
        if (isEmployee) {
            tickets = ticketService.findTicketsByCreator(username);
        } else {
            tickets = ticketService.findAllTickets();
        }

        model.addAttribute("tickets", tickets);
        return "tickets/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("allImportances", Importance.values()); // ДОБАВЛЕНО: Передаем все значения Importance
        return "tickets/create";
    }

    @PostMapping
    public String createTicket(@ModelAttribute Ticket ticket) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        // Теперь createTicket должен принимать Importance
        ticketService.createTicket(ticket.getTitle(), ticket.getDescription(), username, ticket.getImportance());
        return "redirect:/tickets";
    }

    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Integer id, Model model) {
        Ticket ticket = ticketService.findTicketById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticket Id:" + id));
        model.addAttribute("ticket", ticket);
        model.addAttribute("allStatuses", Ticket.Status.values());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            List<User> executors = userRepository.findByRole_NameIn(Arrays.asList("ROLE_IT_SUPPORT", "ROLE_ADMIN"));
            model.addAttribute("executors", executors);
        }

        return "tickets/view";
    }

    @PostMapping("/{id}/update-status")
    public String updateTicketStatus(@PathVariable Integer id, @RequestParam Ticket.Status status) {
        ticketService.updateTicketStatus(id, status);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/assign-to-me")
    public String assignToMe(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        ticketService.assignTicket(id, username);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/assign")
    public String assignTicket(@PathVariable Integer id, @RequestParam(required = false) Integer executorId) {
        if (executorId == null) {
            ticketService.unassignTicket(id);
        } else {
            User executor = userRepository.findById(executorId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid executor Id:" + executorId));
            ticketService.assignTicket(id, executor.getUsername());
        }
        return "redirect:/tickets/" + id;
    }
}
