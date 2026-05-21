package com.example.helpdesk.dto;

import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.dto.AttachmentDto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class TicketDto {
    private Integer id;
    private String title;
    private String description;
    private String importance;
    private String urgency;
    private String impact;
    private String category;
    private Double priorityScore;
    private Ticket.Status status;
    private String creatorUsername;
    private String creatorName;
    private String executorUsername;
    private String executorName;
    private String resolution;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private LocalDateTime slaDeadline;
    private List<AttachmentDto> attachments;
    private Map<String, String> dynamicValues; // filterDisplayName -> valueDisplayName
}
