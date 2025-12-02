package com.tienda.gamer.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");
        log.debug("JwtFilter - requestURI={} AuthorizationHeaderPresent={}", requestUri, authHeader != null);

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // No hay token: continuar sin autenticar (endpoints públicos deben seguir funcionando)
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = authHeader.substring(7);
            String username = null;
            try {
                username = jwtService.extractUsername(jwt);
            } catch (Exception e) {
                log.warn("JwtFilter - error extracting username: {}", e.getMessage());
            }

            log.debug("JwtFilter - extracted username={}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails;
                try {
                    userDetails = userDetailsService.loadUserByUsername(username);
                } catch (Exception e) {
                    log.warn("JwtFilter - userDetailsService failed for username={}: {}", username, e.getMessage());
                    userDetails = null;
                }

                if (userDetails != null) {
                    boolean valid = false;
                    try {
                        valid = jwtService.isTokenValid(jwt, userDetails);
                    } catch (Exception e) {
                        log.warn("JwtFilter - token validation threw exception for username={}: {}", username, e.getMessage());
                    }

                    log.debug("JwtFilter - token valid={}, username={}", valid, username);

                    if (valid) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("JwtFilter - authentication set for username={}", username);
                    } else {
                        log.warn("JwtFilter - token invalid or expired for username={}", username);
                        // no lanzamos excepciones para evitar bloquear endpoints públicos
                    }
                }
            }
        } catch (Exception ex) {
            log.error("JwtFilter - unexpected error in filter chain", ex);
            // no rethrow para evitar cortar la cadena de filtros
        }

        filterChain.doFilter(request, response);
    }
}
