package com.example.helpdesk.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "dynamic_filter_values")
@Data
public class DynamicFilterValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id", nullable = false)
    private DynamicFilter filter;

    @Column(name = "value_name", length = 50, nullable = false)
    private String valueName; // e.g., "OFFICE_1"

    @Column(name = "display_name", length = 100, nullable = false)
    private String displayName; // e.g., "Офис на Ленина"

    @Column(name = "weight_value", nullable = false)
    private Double weightValue = 1.0; // The score contribution when this value is selected
}
