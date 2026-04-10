package com.example.helpdesk.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "parameter_value_weights", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"param_name", "value_name"})
})
@Data
public class ParameterValueWeight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "param_name", nullable = false)
    private PriorityParameter paramName;

    @Column(name = "value_name", length = 50, nullable = false)
    private String valueName; // e.g., "LOW", "MEDIUM", "HIGH", "CRITICAL" for Urgency

    @Column(name = "display_name", length = 100)
    private String displayName; // e.g., "Низкая", "Средняя" for Urgency

    @Column(name = "weight_value", nullable = false)
    private Double weightValue;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
