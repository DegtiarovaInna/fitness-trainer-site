package com.fitness.config;

import com.fitness.enums.Role;
import com.fitness.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import com.fitness.models.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfNotExists("admin@example.com", "Admin User", "admin123", Role.ADMIN, "0000000000");
        createUserIfNotExists("dev@example.com", "Dev User", "dev123", Role.DEV, "1111111111");
        createUserIfNotExists("user@example.com", "Regular User", "user123", Role.USER, "2222222222");
        createUserIfNotExists("userpro@example.com", "Pro User", "userpro123", Role.USER_PRO, "3333333333");
    }

    private void createUserIfNotExists(String email, String name, String password, Role role, String phone) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .phoneNumber(phone)
                    .role(role)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
            System.out.printf("✔ User with role %s created: %s / %s%n", role, email, password);
        }
    }
}

