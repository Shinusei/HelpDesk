package com.example.helpdesk.service;

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
}
