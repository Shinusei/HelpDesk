package com.example.helpdesk;

import com.example.helpdesk.domain.PriorityParameter;
import com.example.helpdesk.domain.PriorityWeight;
import com.example.helpdesk.domain.Role;
import com.example.helpdesk.domain.User;
import com.example.helpdesk.repository.PriorityWeightRepository;
import com.example.helpdesk.repository.RoleRepository;
import com.example.helpdesk.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SpringBootApplication
public class HelpDeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelpDeskApplication.class, args);
    }

    @Bean
    @Transactional
    CommandLineRunner initDatabase(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, PriorityWeightRepository priorityWeightRepository) {
        return args -> {
            Role adminRole = createRoleIfNotFound(roleRepository, "ROLE_ADMIN");
            Role itRole = createRoleIfNotFound(roleRepository, "ROLE_IT_SUPPORT");
            Role employeeRole = createRoleIfNotFound(roleRepository, "ROLE_EMPLOYEE");
            Role vipRole = createRoleIfNotFound(roleRepository, "ROLE_VIP");

            createUserIfNotFound(userRepository, passwordEncoder, "admin", "admin", "Admin User", adminRole);
            createUserIfNotFound(userRepository, passwordEncoder, "it", "it", "IT Support User", itRole);
            createUserIfNotFound(userRepository, passwordEncoder, "user", "user", "Employee User", employeeRole);
            createUserIfNotFound(userRepository, passwordEncoder, "vip", "vip", "VIP User", vipRole);

            // Создаем записи PriorityWeight для всех предопределенных параметров
            for (PriorityParameter param : PriorityParameter.values()) {
                double defaultValue = 1.0; // Значение по умолчанию для новых параметров
                String description = "Вес для параметра '" + param.getDisplayName() + "'";

                // Для IMPORTANCE используем 10.0 как начальное значение
                if (param == PriorityParameter.IMPORTANCE) {
                    defaultValue = 10.0;
                }
                createPriorityWeightIfNotFound(priorityWeightRepository, param, defaultValue, description);
            }
        };
    }

    private Role createRoleIfNotFound(RoleRepository roleRepository, String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(name);
            return roleRepository.save(newRole);
        });
    }

    private void createUserIfNotFound(UserRepository userRepository, PasswordEncoder passwordEncoder, String username, String password, String fullName, Role role) {
        userRepository.findByUsername(username).ifPresentOrElse(
                user -> {},
                () -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setPassword(passwordEncoder.encode(password));
                    newUser.setFullName(fullName);
                    newUser.setRole(role);
                    userRepository.save(newUser);
                }
        );
    }

    private void createPriorityWeightIfNotFound(PriorityWeightRepository priorityWeightRepository, PriorityParameter paramName, double weightValue, String description) {
        priorityWeightRepository.findByParamName(paramName).ifPresentOrElse(
                weight -> {}, // Если существует, ничего не делаем
                () -> { // Если не существует, создаем
                    PriorityWeight newWeight = new PriorityWeight();
                    newWeight.setParamName(paramName);
                    newWeight.setWeightValue(weightValue);
                    newWeight.setDescription(description);
                    priorityWeightRepository.save(newWeight);
                }
        );
    }
}
