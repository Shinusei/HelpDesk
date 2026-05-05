package com.example.helpdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicFilterValueDto {
    private Integer id;
    private String valueName;
    private String displayName;
    private Double weightValue;
}
