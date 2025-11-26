package com.tienda.gamer.util;

import com.tienda.gamer.model.User;
import com.tienda.gamer.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUserProvider {

    private final UserRepository userRepository;

    public AuthUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));
    }
}
