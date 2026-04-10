package com.example.helpdesk.controller.admin;

import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.User;
import com.example.helpdesk.dto.TicketDto;
import com.example.helpdesk.repository.UserRepository;
import com.example.helpdesk.service.TicketService;
import lombok.Data;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/tickets")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminTicketRestController {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    public AdminTicketRestController(TicketService ticketService, UserRepository userRepository) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    @PatchMapping("/{id}/assign")
    public TicketDto assignSupport(@PathVariable("id") Integer ticketId,
                                  @RequestBody AssignRequest request,
                                  Authentication authentication) {
        if (request == null || request.getExecutorUsername() == null || request.getExecutorUsername().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "executorUsername is required");
        }

        User executor = userRepository.findByUsername(request.getExecutorUsername().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + request.getExecutorUsername()));

        if (executor.getRole() == null || !"ROLE_IT_SUPPORT".equals(executor.getRole().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only ROLE_IT_SUPPORT can be assigned as executor");
        }

        Ticket ticket = ticketService.assignTicket(ticketId, executor.getUsername(), authentication.getName());
        return TicketDto.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .importance(ticket.getImportance() != null ? ticket.getImportance().name() : null)
                .urgency(ticket.getUrgency() != null ? ticket.getUrgency().name() : null)
                .impact(ticket.getImpact() != null ? ticket.getImpact().name() : null)
                .category(ticket.getCategory() != null ? ticket.getCategory().name() : null)
                .priorityScore(ticket.getPriorityScore())
                .status(ticket.getStatus())
                .creatorUsername(ticket.getCreator() != null ? ticket.getCreator().getUsername() : null)
                .creatorName(ticket.getCreator() != null ? ticket.getCreator().getFullName() : null)
                .executorUsername(ticket.getExecutor() != null ? ticket.getExecutor().getUsername() : null)
                .executorName(ticket.getExecutor() != null ? ticket.getExecutor().getFullName() : "Не назначен")
                .resolution(ticket.getResolution())
                .createdAt(ticket.getCreatedAt())
                .closedAt(ticket.getClosedAt())
                .slaDeadline(ticket.getSlaDeadline())
                .attachments(null)
                .build();
    }

    @GetMapping("/by-executor")
    public List<TicketDto> ticketsByExecutor(@RequestParam("username") String executorUsername,
                                             @RequestParam(name = "includeClosed", defaultValue = "false") boolean includeClosed,
                                             @RequestParam(name = "sort", defaultValue = "priorityScore") String sort,
                                             @RequestParam(name = "dir", defaultValue = "desc") String dir) {
        Sort sortObj = dir.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();
        return ticketService.findTicketsByExecutorUsername(executorUsername, includeClosed, sortObj)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private TicketDto mapToDto(Ticket ticket) {
        return TicketDto.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .importance(ticket.getImportance() != null ? ticket.getImportance().name() : null)
                .urgency(ticket.getUrgency() != null ? ticket.getUrgency().name() : null)
                .impact(ticket.getImpact() != null ? ticket.getImpact().name() : null)
                .category(ticket.getCategory() != null ? ticket.getCategory().name() : null)
                .priorityScore(ticket.getPriorityScore())
                .status(ticket.getStatus())
                .creatorUsername(ticket.getCreator() != null ? ticket.getCreator().getUsername() : null)
                .creatorName(ticket.getCreator() != null ? ticket.getCreator().getFullName() : null)
                .executorUsername(ticket.getExecutor() != null ? ticket.getExecutor().getUsername() : null)
                .executorName(ticket.getExecutor() != null ? ticket.getExecutor().getFullName() : "Не назначен")
                .resolution(ticket.getResolution())
                .createdAt(ticket.getCreatedAt())
                .closedAt(ticket.getClosedAt())
                .slaDeadline(ticket.getSlaDeadline())
                .attachments(null)
                .build();
    }

    @Data
    public static class AssignRequest {
        private String executorUsername;
    }
}

