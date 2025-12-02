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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro y login con JWT")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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
            log.info("Register attempt - username already exists: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
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
        log.info("User registered id={} username={}", saved.getId(), saved.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved.getId());
    }

    // ================================
    // LOGIN
    // ================================
    @PostMapping("/login")
    @Operation(summary = "Login de usuario, devuelve JWT")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("AuthController.login invoked - username={}", request != null ? request.getUsername() : "null");

        try {
            if (request == null || request.getUsername() == null || request.getPassword() == null) {
                log.warn("AuthController.login - missing credentials in request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            User user = userRepository.findByUsername(request.getUsername()).orElse(null);

            if (user == null) {
                log.warn("AuthController.login - user not found: {}", request.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("AuthController.login - invalid password for user: {}", request.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Cargamos UserDetails para usar en JwtService
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

            String roleName = (user.getRole() != null && user.getRole().getName() != null)
                    ? user.getRole().getName()
                    : "ROLE_USER";

            // LLAMADA CORRECTA: siempre usar la firma de 2 argumentos
            String token = jwtService.generateToken(userDetails, roleName);

            LoginResponse response = new LoginResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    roleName
            );

            log.info("AuthController.login success - username={}, tokenLen={}", user.getUsername(), token != null ? token.length() : 0);
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("AuthController.login - unexpected error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
