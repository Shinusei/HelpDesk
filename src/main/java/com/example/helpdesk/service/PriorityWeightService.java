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

    public PriorityWeightService(PriorityWeightRepository priorityWeightRepository) {
        this.priorityWeightRepository = priorityWeightRepository;
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
        return priorityWeightRepository.save(priorityWeight);
    }

    @Transactional
    public void deletePriorityWeight(Integer id) {
        priorityWeightRepository.deleteById(id);
    }
}
