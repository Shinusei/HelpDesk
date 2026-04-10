package com.example.helpdesk.repository;

import com.example.helpdesk.domain.ParameterValueWeight;
import com.example.helpdesk.domain.PriorityParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParameterValueWeightRepository extends JpaRepository<ParameterValueWeight, Integer> {
    List<ParameterValueWeight> findByParamName(PriorityParameter paramName);
    Optional<ParameterValueWeight> findByParamNameAndValueName(PriorityParameter paramName, String valueName);
}
