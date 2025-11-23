package com.miempresa.config;

import com.miempresa.config.SimpleTokenAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final SimpleTokenAuthFilter simpleTokenAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // nueva forma no-deprecada para csrf
                .csrf(csrf -> csrf.disable())

                // CORS: permitir frontend (ajusta origen si tu front corre en otra URL)
                .cors(cors -> cors.configurationSource(request -> {
                    var cfg = new org.springframework.web.cors.CorsConfiguration();
                    cfg.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
                    cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
                    cfg.setAllowedHeaders(java.util.List.of("*"));
                    cfg.setAllowCredentials(true);
                    cfg.setMaxAge(3600L);
                    return cfg;
                }))

// Autorizaciones
                .authorizeHttpRequests(auth -> auth
                        // 1. Permitir la ruta de Autenticación (login, etc.)
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. EXCEPCIÓN: Permitir el POST para crear cuentas
                        // Esto permite solicitudes POST a /api/accounts sin autenticación.
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/accounts").permitAll()

                        // 3. Proteger TODAS las otras solicitudes a /api/accounts/**
                        // Esto protege GET, PUT, DELETE, y cualquier otra cosa que no sea el POST de creación.
                        .requestMatchers("/api/accounts/**").authenticated()

                        // 4. Permitir cualquier otra solicitud que no caiga en las reglas anteriores
                        .anyRequest().permitAll()
                )

                // stateless API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Manejo de excepciones (devuelven JSON en vez de HTML, evita "response already committed")
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::sendUnauthorizedJson)
                        .accessDeniedHandler(this::sendAccessDeniedJson)
                );

        // Registrar el filtro antes del filtro de username/password para que el token
        // pueble SecurityContext antes de la autorización.
        http.addFilterBefore(simpleTokenAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Responder 401 JSON
    private void sendUnauthorizedJson(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = String.format("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}", authException.getMessage());
        response.getWriter().write(body);
    }

    // Responder 403 JSON
    private void sendAccessDeniedJson(HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = String.format("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"%s\"}", accessDeniedException.getMessage());
        response.getWriter().write(body);
    }
}
