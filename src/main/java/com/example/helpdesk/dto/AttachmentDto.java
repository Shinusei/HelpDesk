package com.example.helpdesk.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttachmentDto {
    private Integer id;
    private String fileName;
    private String fileType;
    private long fileSize;
}
