package com.example.helpdesk.service;

import com.example.helpdesk.domain.User;
import com.example.helpdesk.domain.Role;
import com.example.helpdesk.repository.UserRepository;
import com.example.helpdesk.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User update(Integer id, String fullName, Integer roleId, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        user.setFullName(fullName);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
        user.setRole(role);
        
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Integer id, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("Вы не можете удалить свою собственную учетную запись.");
        }
        
        userRepository.delete(user);
    }
}
