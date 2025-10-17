package com.miempresa.controller;

import com.miempresa.entity.SimpleToken;
import com.miempresa.entity.UserEntity;
import com.miempresa.repository.UserRepository;
import com.miempresa.service.SimpleTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthControllerByEmail {

    private final UserRepository userRepository;
    private final SimpleTokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing email or password"));
        }

        // Tu repo actual devuelve UserEntity (no Optional) — manejar null
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        // Vulnerabilidad intencional: password en texto plano comparado directamente
        if (!password.equals(user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        // Generar token predecible "TOKEN-"+email
        SimpleToken token = tokenService.createTokenForUser(user);

        return ResponseEntity.ok(Map.of(
                "token", token.getToken(),
                "userId", user.getId(),
                "email", user.getEmail()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing Authorization header"));
        }
        String token = authHeader.substring(7);
        tokenService.deleteByToken(token);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
