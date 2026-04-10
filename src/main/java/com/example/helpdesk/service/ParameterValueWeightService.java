package com.example.helpdesk.service;

import com.example.helpdesk.domain.*;
import com.example.helpdesk.repository.ParameterValueWeightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ParameterValueWeightService {

    private final ParameterValueWeightRepository repository;
    private final TicketService ticketService;

    public ParameterValueWeightService(ParameterValueWeightRepository repository, 
                                      @org.springframework.context.annotation.Lazy TicketService ticketService) {
        this.repository = repository;
        this.ticketService = ticketService;
    }

    @PostConstruct
    public void init() {
        try {
            initializeDefaults();
        } catch (Exception e) {
            // Ignore errors during initialization - may happen if table already has data
        }
    }

    @Transactional(readOnly = true)
    public List<ParameterValueWeight> findByParamName(PriorityParameter paramName) {
        return repository.findByParamName(paramName);
    }

    @Transactional(readOnly = true)
    public List<ParameterValueWeight> findAll() {
        return repository.findAll();
    }

    @Transactional
    public ParameterValueWeight updateWeight(Integer id, Double weightValue) {
        ParameterValueWeight weight = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Weight not found: " + id));
        weight.setWeightValue(weightValue);
        weight.setUpdatedAt(LocalDateTime.now());
        ParameterValueWeight saved = repository.save(weight);
        ticketService.recalculateAllPriorities();
        return saved;
    }

    @Transactional
    public ParameterValueWeight save(ParameterValueWeight weight) {
        weight.setUpdatedAt(LocalDateTime.now());
        ParameterValueWeight saved = repository.save(weight);
        ticketService.recalculateAllPriorities();
        return saved;
    }

    @Transactional
    public void initializeDefaults() {
        // Initialize Urgency values
        initializeIfMissing(PriorityParameter.URGENCY, Arrays.asList(
            new ValueWeight("LOW", "Низкая", 1.0),
            new ValueWeight("MEDIUM", "Средняя", 2.0),
            new ValueWeight("HIGH", "Высокая", 3.0),
            new ValueWeight("CRITICAL", "Критическая", 4.0)
        ));

        // Initialize Importance values
        initializeIfMissing(PriorityParameter.IMPORTANCE, Arrays.asList(
            new ValueWeight("LOW", "Низкая", 1.0),
            new ValueWeight("MEDIUM", "Средняя", 2.0),
            new ValueWeight("HIGH", "Высокая", 3.0)
        ));

        // Initialize Impact values
        initializeIfMissing(PriorityParameter.IMPACT, Arrays.asList(
            new ValueWeight("USER", "Один пользователь", 1.0),
            new ValueWeight("DEPARTMENT", "Отдел", 2.0),
            new ValueWeight("COMPANY", "Вся компания", 3.0)
        ));

        // Initialize Category values
        initializeIfMissing(PriorityParameter.CATEGORY, Arrays.asList(
            new ValueWeight("HARDWARE", "Оборудование", 1.0),
            new ValueWeight("SOFTWARE", "ПО", 1.0),
            new ValueWeight("NETWORK", "Сеть", 1.0),
            new ValueWeight("ACCESS", "Доступы", 1.0),
            new ValueWeight("OTHER", "Другое", 0.5)
        ));

        // Initialize Creator Role values
        initializeIfMissing(PriorityParameter.CREATOR_ROLE, Arrays.asList(
            new ValueWeight("USER", "Обычный пользователь", 1.0),
            new ValueWeight("MANAGER", "Менеджер", 2.0),
            new ValueWeight("DIRECTOR", "Директор", 3.0),
            new ValueWeight("EXECUTIVE", "Руководство", 4.0)
        ));

        // Initialize Newer Unresolved Tickets values
        initializeIfMissing(PriorityParameter.NEWER_UNRESOLVED_TICKETS, Arrays.asList(
            new ValueWeight("NONE", "Нет (0)", 0.0),
            new ValueWeight("FEW", "Мало (1-3)", 1.0),
            new ValueWeight("SEVERAL", "Несколько (4-10)", 2.0),
            new ValueWeight("MANY", "Много (>10)", 3.0)
        ));

        // Initialize Waiting Hours values
        initializeIfMissing(PriorityParameter.WAITING_HOURS, Arrays.asList(
            new ValueWeight("FRESH", "< 4 часов", 1.0),
            new ValueWeight("PENDING", "4-24 часа", 2.0),
            new ValueWeight("OVERDUE", "1-3 дня", 3.0),
            new ValueWeight("CRITICAL", "> 3 дней", 4.0)
        ));
    }

    private void initializeIfMissing(PriorityParameter paramName, List<ValueWeight> values) {
        for (ValueWeight vw : values) {
            if (!repository.findByParamNameAndValueName(paramName, vw.valueName).isPresent()) {
                ParameterValueWeight weight = new ParameterValueWeight();
                weight.setParamName(paramName);
                weight.setValueName(vw.valueName);
                weight.setDisplayName(vw.displayName);
                weight.setWeightValue(vw.weightValue);
                repository.save(weight);
            }
        }
    }

    private static class ValueWeight {
        String valueName;
        String displayName;
        Double weightValue;

        ValueWeight(String valueName, String displayName, Double weightValue) {
            this.valueName = valueName;
            this.displayName = displayName;
            this.weightValue = weightValue;
        }
    }
}
