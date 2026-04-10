package com.example.helpdesk.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentDto {
    private Integer id;
    private String text;
    private String authorName;
    private LocalDateTime createdAt;
    private List<AttachmentDto> attachments;
}
