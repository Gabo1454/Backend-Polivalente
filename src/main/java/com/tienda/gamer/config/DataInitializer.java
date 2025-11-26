package com.tienda.gamer.config;

import com.tienda.gamer.model.Role;
import com.tienda.gamer.model.User;
import com.tienda.gamer.repository.RoleRepository;
import com.tienda.gamer.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepo,
                                      UserRepository userRepo,
                                      PasswordEncoder encoder) {
        return args -> {
            Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_ADMIN").build()));
            Role userRole = roleRepo.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_USER").build()));

            if (userRepo.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(encoder.encode("admin123"))
                        .fullName("Administrador")
                        .role(adminRole)
                        .build();
                userRepo.save(admin);
            }
        };
    }
}
