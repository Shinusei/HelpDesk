package com.example.helpdesk.controller;

import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketAttachment;
import com.example.helpdesk.domain.TicketComment;
import com.example.helpdesk.dto.*;
import com.example.helpdesk.service.TicketService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {

    private final TicketService ticketService;

    public TicketRestController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketDto> listTickets(Authentication authentication,
                                      @RequestParam(name = "sort", defaultValue = "priorityScore") String sort,
                                      @RequestParam(name = "dir", defaultValue = "desc") String dir) {
        String username = authentication.getName();
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE") || a.getAuthority().equals("ROLE_VIP"));

        Sort sortObj = dir.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();

        List<Ticket> tickets;
        if (isEmployee) {
            tickets = ticketService.findTicketsByCreator(username, sortObj);
        } else {
            tickets = ticketService.findAllTickets(sortObj);
        }

        return tickets.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TicketDto getTicket(@PathVariable("id") Integer id, Authentication authentication) {
        Ticket ticket = ticketService.findTicketById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
        TicketDto dto = mapToDto(ticket);
        // Populate attachments
        List<AttachmentDto> attachments = ticketService.getAttachments(ticket).stream()
                .map(this::mapAttachment)
                .collect(Collectors.toList());
        dto.setAttachments(attachments);
        return dto;
    }

    @PostMapping
    public TicketDto createTicket(@RequestBody CreateTicketRequest request, Authentication authentication) {
        Ticket ticket = ticketService.createTicket(
                request.getTitle(),
                request.getDescription(),
                authentication.getName(),
                request.getImportance(),
                request.getUrgency(),
                request.getImpact(),
                request.getCategory()
        );
        return mapToDto(ticket);
    }

    @PatchMapping("/{id}/status")
    public TicketDto updateStatus(@PathVariable("id") Integer id, @RequestBody UpdateStatusRequest request, Authentication authentication) {
        // Only support and admin can close/reopen; users can only close their own (handled in service)
        Ticket ticket = ticketService.updateTicketStatus(id, request.getStatus(), authentication.getName(), request.getResolution());
        return mapToDto(ticket);
    }

    @GetMapping("/{id}/comments")
    public List<CommentDto> getComments(@PathVariable("id") Integer id) {
        Ticket ticket = ticketService.findTicketById(id).orElseThrow();
        return ticketService.getComments(ticket).stream()
                .map(c -> {
                    List<AttachmentDto> attachments = c.getAttachments().stream()
                            .map(this::mapAttachment)
                            .collect(Collectors.toList());
                    return CommentDto.builder()
                            .id(c.getId())
                            .text(c.getText())
                            .authorName(c.getAuthor().getFullName())
                            .createdAt(c.getCreatedAt())
                            .attachments(attachments)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/comments")
    public CommentDto addComment(@PathVariable("id") Integer id, @RequestBody CommentRequest request, Authentication authentication) {
        TicketComment comment = ticketService.addComment(id, authentication.getName(), request.getText());
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getFullName())
                .createdAt(comment.getCreatedAt())
                .attachments(List.of())
                .build();
    }

    @PatchMapping("/{id}/assign-to-me")
    @PreAuthorize("hasAnyRole('ROLE_IT_SUPPORT', 'ROLE_ADMIN')")
    public TicketDto assignToMe(@PathVariable("id") Integer id, Authentication authentication) {
        Ticket ticket = ticketService.assignTicket(id, authentication.getName(), authentication.getName());
        return mapToDto(ticket);
    }

    // --- Attachment Endpoints ---

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AttachmentDto uploadAttachment(@PathVariable("id") Integer id,
                                         @RequestParam("file") MultipartFile file,
                                         Authentication authentication) throws IOException {
        TicketAttachment attachment = ticketService.addAttachment(id, file, authentication.getName());
        return mapAttachment(attachment);
    }

    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable("attachmentId") Integer attachmentId) {
        TicketAttachment attachment = ticketService.getAttachment(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + attachmentId));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getFileType() != null ? attachment.getFileType() : "application/octet-stream"))
                .body(attachment.getData());
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable("attachmentId") Integer attachmentId, Authentication authentication) {
        ticketService.deleteAttachment(attachmentId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/comments/{commentId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AttachmentDto uploadCommentAttachment(@PathVariable("id") Integer ticketId,
                                                @PathVariable("commentId") Integer commentId,
                                                @RequestParam("file") MultipartFile file,
                                                Authentication authentication) throws IOException {
        com.example.helpdesk.domain.TicketComment comment = ticketService.getComments(
                ticketService.findTicketById(ticketId).orElseThrow()
        ).stream().filter(c -> c.getId().equals(commentId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        TicketAttachment attachment = ticketService.addCommentAttachment(ticketId, file, authentication.getName(), comment);
        return mapAttachment(attachment);
    }

    // --- Mapping helpers ---

    private AttachmentDto mapAttachment(TicketAttachment a) {
        return AttachmentDto.builder()
                .id(a.getId())
                .fileName(a.getFileName())
                .fileType(a.getFileType())
                .fileSize(a.getData() != null ? a.getData().length : 0)
                .build();
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
                .creatorName(ticket.getCreator().getFullName())
                .executorName(ticket.getExecutor() != null ? ticket.getExecutor().getFullName() : "Не назначен")
                .resolution(ticket.getResolution())
                .createdAt(ticket.getCreatedAt())
                .closedAt(ticket.getClosedAt())
                .slaDeadline(ticket.getSlaDeadline())
                .build();
    }
}
