package com.example.helpdesk.repository;

import com.example.helpdesk.domain.DynamicFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DynamicFilterRepository extends JpaRepository<DynamicFilter, Integer> {
    List<DynamicFilter> findByIsActiveTrue();
}
