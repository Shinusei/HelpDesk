package com.example.helpdesk.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ticket_dynamic_values")
@Data
public class TicketDynamicValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id", nullable = false)
    private DynamicFilter filter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "value_id", nullable = false)
    private DynamicFilterValue value;
}
