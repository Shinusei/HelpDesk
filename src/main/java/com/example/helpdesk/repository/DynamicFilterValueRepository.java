package com.example.helpdesk.repository;

import com.example.helpdesk.domain.DynamicFilterValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DynamicFilterValueRepository extends JpaRepository<DynamicFilterValue, Integer> {
}
