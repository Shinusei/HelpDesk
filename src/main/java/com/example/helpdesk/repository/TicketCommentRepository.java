package com.example.helpdesk.repository;

import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Integer> {
    List<TicketComment> findByTicketOrderByCreatedAtAsc(Ticket ticket);
    
    @org.springframework.data.jpa.repository.Query("SELECT c FROM TicketComment c LEFT JOIN FETCH c.attachments WHERE c.ticket = :ticket ORDER BY c.createdAt ASC")
    List<TicketComment> findByTicketWithAttachmentsOrderByCreatedAtAsc(@org.springframework.data.repository.query.Param("ticket") Ticket ticket);
}
