package com.example.helpdesk.controller;

import com.example.helpdesk.domain.Category;
import com.example.helpdesk.domain.Impact;
import com.example.helpdesk.domain.Importance;
import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketAttachment;
import com.example.helpdesk.domain.TicketComment;
import com.example.helpdesk.domain.Urgency;
import com.example.helpdesk.domain.User;
import com.example.helpdesk.repository.UserRepository;
import com.example.helpdesk.service.TicketService;
import org.springframework.data.domain.Sort;
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
    public String listTickets(Model model,
                              @RequestParam(name = "sort", defaultValue = "priorityScore") String sort,
                              @RequestParam(name = "dir", defaultValue = "desc") String dir) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        Sort sortObj = dir.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();

        List<Ticket> tickets;
        if (isEmployee) {
            tickets = ticketService.findTicketsByCreator(username, sortObj);
        } else {
            tickets = ticketService.findAllTickets(sortObj);
        }

        model.addAttribute("tickets", tickets);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
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
    public String createTicket(@ModelAttribute Ticket ticket, @RequestParam(name = "file", required = false) MultipartFile file, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Ticket createdTicket = ticketService.createTicket(ticket.getTitle(), ticket.getDescription(), username, ticket.getImportance(), ticket.getUrgency(), ticket.getImpact(), ticket.getCategory());
        
        if (file != null && !file.isEmpty()) {
            try {
                ticketService.addAttachment(createdTicket.getId(), file, username);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Заявка создана, но ошибка при загрузке файла: " + e.getMessage());
            }
        }
        
        return "redirect:/tickets";
    }

    @GetMapping("/{id}")
    public String viewTicket(@PathVariable("id") Integer id, Model model) {
        Ticket ticket = ticketService.findTicketById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID заявки: " + id));
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
    public String updateTicketStatus(@PathVariable("id") Integer id, @RequestParam("status") Ticket.Status status, @RequestParam(name = "resolution", required = false) String resolution, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
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
    public String addComment(@PathVariable("id") Integer id, @RequestParam("text") String text, @RequestParam(name = "file", required = false) MultipartFile file, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        TicketComment comment = ticketService.addComment(id, username, text);
        
        if (file != null && !file.isEmpty()) {
            try {
                ticketService.addCommentAttachment(id, file, username, comment);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Комментарий добавлен, но ошибка при загрузке файла: " + e.getMessage());
            }
        }
        
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/attachments")
    public String addAttachment(@PathVariable("id") Integer id, @RequestParam("file") MultipartFile file, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try {
            ticketService.addAttachment(id, file, username);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки файла: " + e.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/attachments/{attachmentId}/delete")
    public String deleteAttachment(@PathVariable("id") Integer id, @PathVariable("attachmentId") Integer attachmentId, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try {
            ticketService.deleteAttachment(attachmentId, username);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    @GetMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable("id") Integer id, @PathVariable("attachmentId") Integer attachmentId) {
        TicketAttachment attachment = ticketService.getAttachment(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid attachment Id:" + attachmentId));
        
        // В реальном приложении нужно проверять, принадлежит ли attachment запрашиваемому ticket'у
        if (!attachment.getTicket().getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getFileType() != null ? attachment.getFileType() : "application/octet-stream"))
                .body(attachment.getData());
    }

    @PostMapping("/{id}/assign-to-me")
    public String assignToMe(@PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        ticketService.assignTicket(id, username, username);
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/{id}/assign")
    public String assignTicket(@PathVariable("id") Integer id, @RequestParam(name = "executorId", required = false) Integer executorId) {
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
