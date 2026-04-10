package com.example.helpdesk.controller.admin;

import com.example.helpdesk.domain.PriorityWeight;
import com.example.helpdesk.service.PriorityWeightService;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/priority-weights")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class PriorityRestController {

    private final PriorityWeightService priorityWeightService;

    public PriorityRestController(PriorityWeightService priorityWeightService) {
        this.priorityWeightService = priorityWeightService;
    }

    @GetMapping
    public List<PriorityWeight> getAllWeights() {
        return priorityWeightService.findAllPriorityWeights();
    }

    @PatchMapping("/{id}")
    public PriorityWeight updateWeight(@PathVariable Integer id, @RequestBody WeightRequest request) {
        PriorityWeight weight = priorityWeightService.findPriorityWeightById(id).orElseThrow();
        weight.setWeightValue(request.getWeight());
        return priorityWeightService.savePriorityWeight(weight);
    }

    @Data
    public static class WeightRequest {
        private Double weight;
    }
}
