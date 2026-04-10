package com.example.helpdesk.controller.admin;

import com.example.helpdesk.domain.ParameterValueWeight;
import com.example.helpdesk.domain.PriorityParameter;
import com.example.helpdesk.dto.ParameterValueWeightDto;
import com.example.helpdesk.service.ParameterValueWeightService;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/parameter-values")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ParameterValueWeightRestController {

    private final ParameterValueWeightService service;

    public ParameterValueWeightRestController(ParameterValueWeightService service) {
        this.service = service;
    }

    @GetMapping
    public List<ParameterValueWeightDto> getAll() {
        return service.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/by-param")
    public List<ParameterValueWeightDto> getAllByParam(@RequestParam String paramName) {
        try {
            PriorityParameter param = PriorityParameter.valueOf(paramName);
            return service.findByParamName(param).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @PatchMapping("/{id}")
    public ParameterValueWeightDto update(@PathVariable Integer id, @RequestBody WeightValueRequest request) {
        return toDto(service.updateWeight(id, request.getWeightValue()));
    }

    @PostMapping("/init")
    public String initializeDefaults() {
        service.initializeDefaults();
        return "Initialized";
    }

    private ParameterValueWeightDto toDto(ParameterValueWeight w) {
        return ParameterValueWeightDto.builder()
                .id(w.getId())
                .paramName(w.getParamName().name())
                .valueName(w.getValueName())
                .displayName(w.getDisplayName())
                .weightValue(w.getWeightValue())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    @Data
    public static class WeightValueRequest {
        private Double weightValue;
    }
}
