package com.example.helpdesk.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING) // ДОБАВЛЕНО
    @Column(name = "importance", length = 20, nullable = false) // ДОБАВЛЕНО
    private Importance importance; // ДОБАВЛЕНО

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", length = 20, nullable = false)
    private Urgency urgency = Urgency.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "impact", length = 20, nullable = false)
    private Impact impact = Impact.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20, nullable = false)
    private Category category = Category.OTHER;

    @Column(name = "priority_score")
    private Double priorityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor_id")
    private User executor;

    @Column(name = "resolution", columnDefinition = "text")
    private String resolution;

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public enum Status {
        NEW("Новый"),
        IN_PROGRESS("В работе"),
        CLOSED("Закрыт");

        private final String displayName;
        Status(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
