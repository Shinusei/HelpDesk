package com.example.helpdesk.service;

import com.example.helpdesk.domain.PriorityParameter;
import com.example.helpdesk.domain.PriorityWeight;
import com.example.helpdesk.repository.PriorityWeightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PriorityWeightService {

    private final PriorityWeightRepository priorityWeightRepository;
    private final com.example.helpdesk.service.TicketService ticketService;

    public PriorityWeightService(PriorityWeightRepository priorityWeightRepository, @org.springframework.context.annotation.Lazy com.example.helpdesk.service.TicketService ticketService) {
        this.priorityWeightRepository = priorityWeightRepository;
        this.ticketService = ticketService;
    }

    @Transactional(readOnly = true)
    public List<PriorityWeight> findAllPriorityWeights() {
        return priorityWeightRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PriorityWeight> findPriorityWeightById(Integer id) {
        return priorityWeightRepository.findById(id);
    }

    @Transactional
    public PriorityWeight savePriorityWeight(PriorityWeight priorityWeight) {
        priorityWeight.setUpdatedAt(LocalDateTime.now());
        PriorityWeight saved = priorityWeightRepository.save(priorityWeight);
        ticketService.recalculateAllPriorities();
        return saved;
    }

    @Transactional
    public void deletePriorityWeight(Integer id) {
        priorityWeightRepository.deleteById(id);
    }

    @Transactional
    public List<PriorityWeight> resetToDefaults() {
        // Базовые значения весов
        double[] defaultWeights = {
            1.0,  // IMPORTANCE
            4.0,  // NEWER_UNRESOLVED_TICKETS
            3.0,  // URGENCY
            2.0,  // IMPACT
            0.5,  // CATEGORY
            0.5,  // CREATOR_ROLE
            1.5   // WAITING_HOURS
        };

        PriorityParameter[] params = PriorityParameter.values();
        
        for (int i = 0; i < params.length && i < defaultWeights.length; i++) {
            final int index = i;
            PriorityParameter param = params[index];
            Optional<PriorityWeight> existing = priorityWeightRepository.findByParamName(param);
            
            PriorityWeight weight;
            if (existing.isPresent()) {
                weight = existing.get();
            } else {
                weight = new PriorityWeight();
                weight.setParamName(param);
            }
            
            weight.setWeightValue(defaultWeights[index]);
            weight.setDescription(null);
            weight.setUpdatedAt(LocalDateTime.now());
            priorityWeightRepository.save(weight);
        }
        
        ticketService.recalculateAllPriorities();
        return findAllPriorityWeights();
    }
}
