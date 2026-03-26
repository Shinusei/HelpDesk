package com.example.helpdesk.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "priority_weights", uniqueConstraints = {@UniqueConstraint(columnNames = {"param_name"})}) // Добавлено ограничение уникальности
@Data
public class PriorityWeight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING) // ИСПРАВЛЕНО: теперь это enum
    @Column(name = "param_name", length = 100, nullable = false)
    private PriorityParameter paramName; // ИСПРАВЛЕНО: тип PriorityParameter

    @Column(name = "weight_value", nullable = false)
    private Double weightValue;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
