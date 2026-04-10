package com.example.helpdesk.controller.admin;

import com.example.helpdesk.domain.User;
import com.example.helpdesk.domain.Role;
import com.example.helpdesk.dto.UserDto;
import com.example.helpdesk.repository.RoleRepository;
import com.example.helpdesk.service.UserService;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserAdminRestController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserAdminRestController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Integer id) {
        return userService.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @PostMapping
    public UserDto createUser(@RequestBody UserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setPassword(request.getPassword());
        Role role = roleRepository.findById(request.getRoleId()).orElseThrow();
        user.setRole(role);
        return mapToDto(userService.save(user));
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable Integer id, @RequestBody UserRequest request) {
        return mapToDto(userService.update(id, request.getFullName(), request.getRoleId(), request.getPassword()));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id, Authentication authentication) {
        userService.delete(id, authentication.getName());
    }

    @GetMapping("/roles")
    public List<RoleDto> getRoles() {
        return userService.findAllRoles().stream()
                .map(r -> new RoleDto(r.getId(), r.getName()))
                .collect(Collectors.toList());
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .build();
    }

    @Data
    public static class UserRequest {
        private String username;
        private String fullName;
        private String password;
        private Integer roleId;
    }

    @Data
    public static class RoleDto {
        private final Integer id;
        private final String name;
    }
}
