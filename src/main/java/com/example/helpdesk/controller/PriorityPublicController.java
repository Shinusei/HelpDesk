package com.example.helpdesk.controller;

import com.example.helpdesk.dto.PriorityWeightDto;
import com.example.helpdesk.service.PriorityWeightService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/priority-weights")
public class PriorityPublicController {

    private final PriorityWeightService priorityWeightService;

    public PriorityPublicController(PriorityWeightService priorityWeightService) {
        this.priorityWeightService = priorityWeightService;
    }

    @GetMapping
    public List<PriorityWeightDto> listActiveWeights() {
        return priorityWeightService.findAllPriorityWeights().stream()
                .map(w -> PriorityWeightDto.builder()
                        .paramName(w.getParamName().name())
                        .active(w.getActive())
                        .build())
                .collect(Collectors.toList());
    }
}
