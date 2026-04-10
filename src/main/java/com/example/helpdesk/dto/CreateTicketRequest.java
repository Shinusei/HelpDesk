package com.example.helpdesk.dto;

import com.example.helpdesk.domain.Category;
import com.example.helpdesk.domain.Impact;
import com.example.helpdesk.domain.Importance;
import com.example.helpdesk.domain.Urgency;
import lombok.Data;

@Data
public class CreateTicketRequest {
    private String title;
    private String description;
    private Importance importance;
    private Urgency urgency;
    private Impact impact;
    private Category category;
}
