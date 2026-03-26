package com.example.helpdesk.repository;

import com.example.helpdesk.domain.PriorityParameter;
import com.example.helpdesk.domain.PriorityWeight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriorityWeightRepository extends JpaRepository<PriorityWeight, Integer> {
    Optional<PriorityWeight> findByParamName(PriorityParameter paramName);
}
