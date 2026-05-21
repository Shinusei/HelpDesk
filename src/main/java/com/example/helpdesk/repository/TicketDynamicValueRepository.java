package com.example.helpdesk.repository;

import com.example.helpdesk.domain.TicketDynamicValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketDynamicValueRepository extends JpaRepository<TicketDynamicValue, Integer> {
}
