package com.example.helpdesk.repository;

import com.example.helpdesk.domain.Ticket;
import com.example.helpdesk.domain.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Integer> {
    List<TicketAttachment> findByTicketOrderByCreatedAtAsc(Ticket ticket);
    List<TicketAttachment> findByTicketAndCommentIsNullOrderByCreatedAtAsc(Ticket ticket);
}
