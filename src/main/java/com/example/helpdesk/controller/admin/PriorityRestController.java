package com.example.helpdesk.controller.admin;

import com.example.helpdesk.domain.PriorityWeight;
import com.example.helpdesk.dto.PriorityWeightDto;
import com.example.helpdesk.service.PriorityWeightService;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/priority-weights")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
        if (request.getWeightValue() != null) {
            weight.setWeightValue(request.getWeightValue());
        }
        if (request.getDescription() != null) {
            weight.setDescription(request.getDescription());
        }
        return toDto(priorityWeightService.savePriorityWeight(weight));
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
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    @Data
    public static class WeightRequest {
        private Double weightValue;
        private String description;
    }
}
