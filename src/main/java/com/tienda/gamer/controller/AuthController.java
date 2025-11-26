package com.tienda.gamer.controller;

import com.tienda.gamer.dto.LoginRequest;
import com.tienda.gamer.dto.LoginResponse;
import com.tienda.gamer.dto.RegisterRequest;
import com.tienda.gamer.model.Role;
import com.tienda.gamer.model.User;
import com.tienda.gamer.repository.RoleRepository;
import com.tienda.gamer.repository.UserRepository;
import com.tienda.gamer.security.CustomUserDetailsService;
import com.tienda.gamer.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro y login con JWT")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // ================================
    // REGISTRO
    // ================================
    @PostMapping("/register")
    @Operation(summary = "Registrar usuario (rol USER)")
    public ResponseEntity<Long> register(@RequestBody RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .build();
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("ROLE_USER").build()
                ));

        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(userRole)
                .build();

        User saved = userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved.getId());
    }

    // ================================
    // LOGIN
    // ================================
    @PostMapping("/login")
    @Operation(summary = "Login de usuario, devuelve JWT")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails, user.getRole().getName());

        LoginResponse response = new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().getName()
        );

        return ResponseEntity.ok(response);
    }
}
