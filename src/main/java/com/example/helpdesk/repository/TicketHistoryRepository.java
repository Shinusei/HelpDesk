package com.example.helpdesk.repository;

import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Integer> {
    List<TicketHistory> findByTicketOrderByCreatedAtDesc(Ticket ticket);
}
