package com.miempresa.controller;


import com.miempresa.entity.UserEntity;
import com.miempresa.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @PersistenceContext
    private EntityManager em;


    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        // INTENCIONAL: endpoint público que devuelve todas las entidades (incluye password y relaciones).
        List<UserEntity> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    // Test (curl):
    // curl http://localhost:8080/api/users
    // Nota corrección: requerir autenticación/roles y devolver DTOs que oculten password.

    // -------------------------
    // GET /api/users/{id}
    // -------------------------
    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Long id) {
        Optional<UserEntity> u = userService.getUserById(id);
        return u.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    // Test (curl):
    // curl http://localhost:8080/api/users/2
    // Nota corrección: verificar que el requester sea owner o admin; usar DTO.

    // -------------------------
    // POST /api/users
    // -------------------------
    @PostMapping
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user) {
        // INTENCIONAL: no hay validaciones ni hashing de password.
        // Propósito educativo: permitir creación de usuarios con rol elegido por el request.
        UserEntity created = userService.createUser(user);
        return ResponseEntity.ok(created);
    }
    // Test (curl):
    // curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d
    // '{"nombre":"Evil","email":"e@e.com","rol":"ADMIN","password":"1234"}'
    // Nota corrección: validar inputs, forzar rol por servidor, hashear password, no devolver password.

    // -------------------------
    // PUT /api/users/{id}
    // -------------------------
    @PutMapping("/{id}")
    ResponseEntity<UserEntity> updateUser(@PathVariable Long id, @RequestBody UserEntity patch) {
        // INTENCIONAL: actualización sin verificación de permisos ni saneamiento.
        UserEntity updated = userService.updateUser(id, patch);
        return ResponseEntity.ok(updated);
    }

    // Test (curl):
    // curl -X PUT http://localhost:8080/api/users/3 -H "Content-Type: application/json" -d '{"rol":"ADMIN","password":"nuevo"}'
    // Nota corrección: chequear owner/admin, evitar cambio de rol por usuario regular, hashear password.

    // -------------------------
    // DELETE /api/users/{id}
    // -------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // INTENCIONAL: eliminación sin control de acceso ni soft-delete.
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Test (curl):
    // curl -X DELETE http://localhost:8080/api/users/4
    // Nota corrección: requerir admin, registrar en audit log, usar soft-delete.

    // -------------------------
    // POST /api/users/search?q=...
    // -------------------------
    @GetMapping("/search")
    public ResponseEntity<List<UserEntity>> search(@RequestParam(name = "q", required = false, defaultValue = "") String q) {
        // INTENCIONAL: pasa el parámetro tal cual al service que usa JPQL concatenado (vulnerable a inyección).
        List<UserEntity> results = userService.searchByQuery(q);
        return ResponseEntity.ok(results);

    }
    // Test (curl):
    // curl "http://localhost:8080/api/users/search?q=' OR '1'='1"
    // Nota corrección: usar consultas parametrizadas y validar/sanitizar input.

    // POST /api/login
    // -------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest lr) {
        try {
            // Coherente con LoginRequest (email)
            String sql = "SELECT id, nombre, password, email FROM uninpahu.user WHERE email = '"
                    + lr.getEmail() + "' AND password = '" + lr.getPassword() + "' LIMIT 1";

            // usar jakarta.persistence.Query (o var) — evita javax.persistence
            jakarta.persistence.Query q = em.createNativeQuery(sql);
            java.util.List<?> results = q.getResultList();
            if (results != null && !results.isEmpty()) {
                Object[] cols = (Object[]) results.get(0);
                java.util.Map<String,Object> user = new java.util.HashMap<>();
                user.put("id", cols[0]);
                user.put("username", cols[1]);
                user.put("email", cols[3]);
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(401).body("Invalid credentials");
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Error during login: " + ex.getMessage());
        }
    }


    // Simple inner class para recibir login payload (mantener simple y vulnerable)
    // Si no usas Lombok, crea getters/setters o usa campos públicos.
    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    // Test (curl):
    // curl -X POST http://localhost:8080/api/users/login -H "Content-Type: application/json" -d '{"email":"lab@example.com","password":"secret"}'
    // Nota corrección: hashear password, limitar intentos, devolver token seguro en vez de entidad.

}