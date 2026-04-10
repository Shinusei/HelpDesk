package com.example.helpdesk.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PriorityWeightDto {
    private Integer id;
    private String paramName;
    private String displayName;
    private Double weightValue;
    private String description;
    private LocalDateTime updatedAt;
}

