package com.example.helpdesk.controller;

import com.example.helpdesk.domain.DynamicFilter;
import com.example.helpdesk.domain.DynamicFilterValue;
import com.example.helpdesk.dto.DynamicFilterDto;
import com.example.helpdesk.dto.DynamicFilterValueDto;
import com.example.helpdesk.repository.DynamicFilterRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dynamic-filters")
public class DynamicFilterPublicController {

    private final DynamicFilterRepository filterRepository;

    public DynamicFilterPublicController(DynamicFilterRepository filterRepository) {
        this.filterRepository = filterRepository;
    }

    @GetMapping
    public List<DynamicFilterDto> listActiveFilters() {
        return filterRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private DynamicFilterDto mapToDto(DynamicFilter f) {
        return DynamicFilterDto.builder()
                .id(f.getId())
                .name(f.getName())
                .displayName(f.getDisplayName())
                .values(f.getValues().stream().map(this::mapValueToDto).collect(Collectors.toList()))
                .build();
    }

    private DynamicFilterValueDto mapValueToDto(DynamicFilterValue v) {
        return DynamicFilterValueDto.builder()
                .id(v.getId())
                .valueName(v.getValueName())
                .displayName(v.getDisplayName())
                .build();
    }
}
