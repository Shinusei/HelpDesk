package com.example.helpdesk.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ParameterValueWeightDto {
    private Integer id;
    private String paramName;
    private String valueName;
    private String displayName;
    private Double weightValue;
    private LocalDateTime updatedAt;
}
