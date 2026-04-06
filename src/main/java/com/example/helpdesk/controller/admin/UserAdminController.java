package com.example.helpdesk.controller.admin;

import com.example.helpdesk.domain.User;
import com.example.helpdesk.domain.Role;
import com.example.helpdesk.service.UserService;
import com.example.helpdesk.repository.RoleRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserAdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserAdminController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", userService.findAllRoles());
        return "admin/users/create";
    }

    @PostMapping
    public String createUser(@ModelAttribute User user, @RequestParam("roleId") Integer roleId, RedirectAttributes redirectAttributes) {
        try {
            Role role = roleRepository.findById(roleId).orElseThrow();
            user.setRole(role);
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "Пользователь успешно создан.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", " Ошибка при создании пользователя: " + e.getMessage());
            return "redirect:/admin/users/create";
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        User user = userService.findById(id).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("roles", userService.findAllRoles());
        return "admin/users/edit";
    }

    @PostMapping("/update")
    public String updateUser(@RequestParam("id") Integer id,
                             @RequestParam("fullName") String fullName,
                             @RequestParam("roleId") Integer roleId,
                             @RequestParam(value = "password", required = false) String password,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.update(id, fullName, roleId, password);
            redirectAttributes.addFlashAttribute("success", "Данные пользователя обновлены.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            userService.delete(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Пользователь удален.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
