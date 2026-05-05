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
    @com.fasterxml.jackson.annotation.JsonProperty("active")
    private Boolean active;
    private LocalDateTime updatedAt;
}

