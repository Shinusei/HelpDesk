package com.example.helpdesk.controller;

import com.example.helpdesk.domain.Category;
import com.example.helpdesk.domain.Impact;
import com.example.helpdesk.domain.Importance;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketAttachment;
import com.example.helpdesk.domain.Urgency;
import com.example.helpdesk.domain.User;
import com.example.helpdesk.repository.UserRepository;
import com.example.helpdesk.service.TicketService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/recommended")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ROLE_IT_SUPPORT', 'ROLE_ADMIN')")
    public String getRecommendedTicket() {
        return ticketService.getRecommendedTicket()
                .map(ticket -> "redirect:/tickets/" + ticket.getId())
                .orElse("redirect:/tickets");
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("allImportances", Importance.values());
        model.addAttribute("allUrgencies", Urgency.values());
        model.addAttribute("allImpacts", Impact.values());
        model.addAttribute("allCategories", Category.values());
        return "tickets/create";
    }

    @PostMapping
    public String createTicket(@ModelAttribute Ticket ticket) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        ticketService.createTicket(ticket.getTitle(), ticket.getDescription(), username, ticket.getImportance(), ticket.getUrgency(), ticket.getImpact(), ticket.getCategory());
        return "redirect:/tickets";
    }

    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Integer id, Model model) {
        Ticket ticket = ticketService.findTicketById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticket Id:" + id));
        model.addAttribute("ticket", ticket);
        model.addAttribute("allStatuses", Ticket.Status.values());
        model.addAttribute("comments", ticketService.getComments(ticket));
        model.addAttribute("history", ticketService.getHistory(ticket));
        model.addAttribute("attachments", ticketService.getAttachments(ticket));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            List<User> executors = userRepository.findByRole_NameIn(Arrays.asList("ROLE_IT_SUPPORT", "ROLE_ADMIN"));
            model.addAttribute("executors", executors);
        }

        return "tickets/view";
    }

    @PostMapping("/{id}/update-status")
    public String updateTicketStatus(@PathVariable Integer id, @RequestParam Ticket.Status status, @RequestParam(required = false) String resolution, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try {
            ticketService.updateTicketStatus(id, status, username, resolution);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Integer id, @RequestParam String text) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        ticketService.addComment(id, username, text);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/attachments")
    public String addAttachment(@PathVariable Integer id, @RequestParam("file") MultipartFile file, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try {
            ticketService.addAttachment(id, file, username);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @GetMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Integer id, @PathVariable Integer attachmentId) {
        TicketAttachment attachment = ticketService.getAttachment(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid attachment Id:" + attachmentId));
        
        // В реальном приложении нужно проверять, принадлежит ли attachment запрашиваемому ticket'у
        if (!attachment.getTicket().getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getFileType() != null ? attachment.getFileType() : "application/octet-stream"))
                .body(attachment.getData());
    }

    @PostMapping("/{id}/assign-to-me")
    public String assignToMe(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        ticketService.assignTicket(id, username, username);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/assign")
    public String assignTicket(@PathVariable Integer id, @RequestParam(required = false) Integer executorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (executorId == null) {
            ticketService.unassignTicket(id, currentUsername);
        } else {
            User executor = userRepository.findById(executorId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid executor Id:" + executorId));
            ticketService.assignTicket(id, executor.getUsername(), currentUsername);
        }
        return "redirect:/tickets/" + id;
    }
}
