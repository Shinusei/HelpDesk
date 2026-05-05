package com.example.helpdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicFilterDto {
    private Integer id;
    private String name;
    private String displayName;
    private Double weight;
    private Boolean isActive;
    private List<DynamicFilterValueDto> values;
}
