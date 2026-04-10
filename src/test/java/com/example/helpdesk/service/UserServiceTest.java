package com.example.helpdesk.service;

import com.example.helpdesk.domain.Role;
import com.example.helpdesk.domain.User;
import com.example.helpdesk.repository.RoleRepository;
import com.example.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private Role userRole;
    private Role adminRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordEncoder);

        userRole = new Role();
        userRole.setId(1);
        userRole.setName("ROLE_USER");

        adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ROLE_ADMIN");

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");
        testUser.setRole(userRole);
    }

    @Test
    void findAll_ReturnsAllUsers() {
        User anotherUser = new User();
        anotherUser.setId(2);
        anotherUser.setUsername("another");
        anotherUser.setFullName("Another User");
        anotherUser.setRole(adminRole);

        when(userRepository.findAll()).thenReturn(List.of(testUser, anotherUser));

        List<User> result = userService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(testUser));
        assertTrue(result.contains(anotherUser));
    }

    @Test
    void findById_ReturnsUser_WhenExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void findById_ReturnsEmpty_WhenNotExists() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999);

        assertFalse(result.isPresent());
    }

    @Test
    void findAllRoles_ReturnsAllRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(userRole, adminRole));

        List<Role> result = userService.findAllRoles();

        assertEquals(2, result.size());
        assertTrue(result.contains(userRole));
        assertTrue(result.contains(adminRole));
    }

    @Test
    void save_EncodesPassword() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("rawPassword");
        newUser.setFullName("New User");
        newUser.setRole(userRole);

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1);
            return u;
        });

        User result = userService.save(newUser);

        assertEquals("encodedPassword", result.getPassword());
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(newUser);
    }

    @Test
    void update_UpdatesFields() {
        Role newRole = new Role();
        newRole.setId(3);
        newRole.setName("ROLE_IT_SUPPORT");

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(3)).thenReturn(Optional.of(newRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.update(1, "Updated Name", 3, null);

        assertEquals("Updated Name", testUser.getFullName());
        assertEquals(newRole, testUser.getRole());
        verify(userRepository).save(testUser);
    }

    @Test
    void update_UpdatesPassword_WhenProvided() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.update(1, null, 1, "newPassword");

        assertEquals("newEncodedPassword", testUser.getPassword());
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    void update_UserNotFound_ThrowsException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                userService.update(999, "Name", 1, null)
        );
    }

    @Test
    void update_RoleNotFound_ThrowsException() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                userService.update(1, "Name", 999, null)
        );
    }

    @Test
    void delete_DeletesUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        userService.delete(1, "anotheruser");

        verify(userRepository).delete(testUser);
    }

    @Test
    void delete_SelfDeletion_ThrowsException() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () ->
                userService.delete(1, "testuser")
        );

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void delete_UserNotFound_ThrowsException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                userService.delete(999, "testuser")
        );
    }
}
