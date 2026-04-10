package com.example.helpdesk.repository;

import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByCreator(User creator, Sort sort);
    List<Ticket> findByCreatorAndStatusNot(User creator, Ticket.Status status, Sort sort);
    long countByStatusNotAndCreatedAtAfter(Ticket.Status status, java.time.LocalDateTime createdAt);
    
    java.util.Optional<Ticket> findFirstByStatusNotAndExecutorIsNullOrderByPriorityScoreDesc(Ticket.Status status);

    List<Ticket> findByExecutor_Username(String executorUsername, Sort sort);
    List<Ticket> findByExecutor_UsernameAndStatusNot(String executorUsername, Ticket.Status status, Sort sort);

    List<Ticket> findByStatusNot(Ticket.Status status, Sort sort);
    List<Ticket> findByExecutorIsNullAndStatusNot(Ticket.Status status, Sort sort);

    long countByStatus(Ticket.Status status);
    long countBySlaDeadlineBeforeAndStatusNot(java.time.LocalDateTime date, Ticket.Status status);
}
