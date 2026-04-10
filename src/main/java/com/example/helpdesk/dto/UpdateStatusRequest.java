package com.example.helpdesk.dto;

import com.example.helpdesk.domain.Ticket;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    private Ticket.Status status;
    private String resolution;
}
