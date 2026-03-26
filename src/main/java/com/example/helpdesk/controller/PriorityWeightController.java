package com.example.helpdesk.controller;

import com.example.helpdesk.domain.PriorityParameter;
import com.example.helpdesk.domain.PriorityWeight;
import com.example.helpdesk.service.PriorityWeightService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/priority-weights")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class PriorityWeightController {

    private final PriorityWeightService priorityWeightService;

    public PriorityWeightController(PriorityWeightService priorityWeightService) {
        this.priorityWeightService = priorityWeightService;
    }

    @GetMapping
    public String listPriorityWeights(Model model) {
        model.addAttribute("weights", priorityWeightService.findAllPriorityWeights());
        return "admin/priority-weights/list";
    }

    // Удаляем метод showCreateForm, так как параметры предопределены и не создаются вручную

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        PriorityWeight weight = priorityWeightService.findPriorityWeightById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid priority weight Id:" + id));
        model.addAttribute("weight", weight);
        // PriorityParameter.values() больше не нужно для выбора, так как paramName не редактируется
        return "admin/priority-weights/form";
    }

    @PostMapping
    public String savePriorityWeight(@ModelAttribute PriorityWeight weight) {
        // Убедимся, что мы обновляем существующий вес, а не создаем новый
        if (weight.getId() == null) {
            throw new IllegalArgumentException("Cannot create new PriorityWeight via this form. Only existing weights can be updated.");
        }
        priorityWeightService.savePriorityWeight(weight);
        return "redirect:/admin/priority-weights";
    }

    // Удаляем метод deletePriorityWeight, так как параметры предопределены и не удаляются
}
