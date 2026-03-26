package com.example.helpdesk.repository;

import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByCreator(User creator);
}
