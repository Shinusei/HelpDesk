package com.example.helpdesk.controller.admin;

import com.example.helpdesk.domain.PriorityWeight;
import com.example.helpdesk.dto.PriorityWeightDto;
import com.example.helpdesk.service.PriorityWeightService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/priority-weights")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Slf4j
public class PriorityRestController {

    private final PriorityWeightService priorityWeightService;

    public PriorityRestController(PriorityWeightService priorityWeightService) {
        this.priorityWeightService = priorityWeightService;
    }

    @GetMapping
    public List<PriorityWeightDto> getAllWeights() {
        return priorityWeightService.findAllPriorityWeights().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{id}")
    public PriorityWeightDto updateWeight(@PathVariable Integer id, @RequestBody WeightRequest request) {
        PriorityWeight weight = priorityWeightService.findPriorityWeightById(id).orElseThrow();
        log.info("Updating weight for param: {}. Current active: {}, Request active: {}", 
                weight.getParamName(), weight.getActive(), request.getActive());
        
        if (request.getWeightValue() != null) {
            weight.setWeightValue(request.getWeightValue());
        }
        if (request.getDescription() != null) {
            weight.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            log.info("Changing active from {} to {}", weight.getActive(), request.getActive());
            weight.setActive(request.getActive());
        }
        PriorityWeight saved = priorityWeightService.savePriorityWeight(weight);
        log.info("Saved weight for param: {}. New active: {}", saved.getParamName(), saved.getActive());
        return toDto(saved);
    }

    @PostMapping("/reset")
    public List<PriorityWeightDto> resetToDefaults() {
        return priorityWeightService.resetToDefaults().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private PriorityWeightDto toDto(PriorityWeight w) {
        return PriorityWeightDto.builder()
                .id(w.getId())
                .paramName(w.getParamName() != null ? w.getParamName().name() : null)
                .displayName(w.getParamName() != null ? w.getParamName().getDisplayName() : null)
                .weightValue(w.getWeightValue())
                .description(w.getDescription())
                .active(w.getActive())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    @Data
    @NoArgsConstructor
    public static class WeightRequest {
        private Double weightValue;
        private String description;
        @JsonProperty("active")
        private Boolean active;
    }
}
