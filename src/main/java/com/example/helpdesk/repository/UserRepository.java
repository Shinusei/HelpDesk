package com.example.helpdesk.repository;

import com.example.helpdesk.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    // Находим пользователей по списку имен ролей
    List<User> findByRole_NameIn(List<String> roleNames);
}
