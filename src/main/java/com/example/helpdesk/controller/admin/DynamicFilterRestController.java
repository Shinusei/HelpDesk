package com.example.helpdesk.controller.admin;

import com.example.helpdesk.domain.DynamicFilter;
import com.example.helpdesk.domain.DynamicFilterValue;
import com.example.helpdesk.dto.DynamicFilterDto;
import com.example.helpdesk.dto.DynamicFilterValueDto;
import com.example.helpdesk.repository.DynamicFilterRepository;
import com.example.helpdesk.repository.DynamicFilterValueRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dynamic-filters")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class DynamicFilterRestController {

    private final DynamicFilterRepository filterRepository;
    private final DynamicFilterValueRepository valueRepository;
    private final com.example.helpdesk.service.TicketService ticketService;

    public DynamicFilterRestController(DynamicFilterRepository filterRepository,
            DynamicFilterValueRepository valueRepository, com.example.helpdesk.service.TicketService ticketService) {
        this.filterRepository = filterRepository;
        this.valueRepository = valueRepository;
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<DynamicFilterDto> listFilters() {
        return filterRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @PostMapping
    public DynamicFilterDto createFilter(@RequestBody DynamicFilterDto dto) {
        DynamicFilter filter = new DynamicFilter();
        filter.setName(dto.getName());
        filter.setDisplayName(dto.getDisplayName());
        filter.setWeight(dto.getWeight() != null ? dto.getWeight() : 1.0);
        filter.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        DynamicFilter saved = filterRepository.save(filter);
        ticketService.recalculateAllPriorities();
        return mapToDto(saved);
    }

    @PatchMapping("/{id}")
    public DynamicFilterDto updateFilter(@PathVariable Integer id, @RequestBody DynamicFilterDto dto) {
        DynamicFilter filter = filterRepository.findById(id).orElseThrow();
        if (dto.getDisplayName() != null)
            filter.setDisplayName(dto.getDisplayName());
        if (dto.getWeight() != null)
            filter.setWeight(dto.getWeight());
        if (dto.getIsActive() != null)
            filter.setIsActive(dto.getIsActive());
        DynamicFilter saved = filterRepository.save(filter);
        ticketService.recalculateAllPriorities();
        return mapToDto(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteFilter(@PathVariable Integer id) {
        filterRepository.deleteById(id);
        ticketService.recalculateAllPriorities();
    }

    @PostMapping("/{id}/values")
    public DynamicFilterValueDto addValue(@PathVariable Integer id, @RequestBody DynamicFilterValueDto dto) {
        DynamicFilter filter = filterRepository.findById(id).orElseThrow();
        DynamicFilterValue value = new DynamicFilterValue();
        value.setFilter(filter);
        value.setValueName(dto.getValueName());
        value.setDisplayName(dto.getDisplayName());
        value.setWeightValue(dto.getWeightValue() != null ? dto.getWeightValue() : 1.0);
        DynamicFilterValue saved = valueRepository.save(value);
        ticketService.recalculateAllPriorities();
        return mapValueToDto(saved);
    }

    @PatchMapping("/values/{valueId}")
    public DynamicFilterValueDto updateValue(@PathVariable Integer valueId, @RequestBody DynamicFilterValueDto dto) {
        DynamicFilterValue value = valueRepository.findById(valueId).orElseThrow();
        if (dto.getDisplayName() != null)
            value.setDisplayName(dto.getDisplayName());
        if (dto.getWeightValue() != null)
            value.setWeightValue(dto.getWeightValue());
        DynamicFilterValue saved = valueRepository.save(value);
        ticketService.recalculateAllPriorities();
        return mapValueToDto(saved);
    }

    @DeleteMapping("/values/{valueId}")
    public void deleteValue(@PathVariable Integer valueId) {
        valueRepository.deleteById(valueId);
        ticketService.recalculateAllPriorities();
    }

    private DynamicFilterDto mapToDto(DynamicFilter f) {
        return DynamicFilterDto.builder()
                .id(f.getId())
                .name(f.getName())
                .displayName(f.getDisplayName())
                .weight(f.getWeight())
                .isActive(f.getIsActive())
                .values(f.getValues().stream().map(this::mapValueToDto).collect(Collectors.toList()))
                .build();
    }

    private DynamicFilterValueDto mapValueToDto(DynamicFilterValue v) {
        return DynamicFilterValueDto.builder()
                .id(v.getId())
                .valueName(v.getValueName())
                .displayName(v.getDisplayName())
                .weightValue(v.getWeightValue())
                .build();
    }
}
