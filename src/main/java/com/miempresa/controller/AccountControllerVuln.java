package com.miempresa.controller;

import com.miempresa.entity.AccountEntity;
import com.miempresa.service.Impl.AccountServiceVuln;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
class AccountControllerVuln {
    private final AccountServiceVuln service;


    // POST /api/accounts -> crea cuenta. INTENCIONAL: client puede fijar usuario en body (vulnerabilidad educativa)
    @PostMapping
    public ResponseEntity<AccountEntity> create(@Valid @RequestBody AccountEntity account) {
        AccountEntity saved = service.createAccount(account);
        return ResponseEntity.ok(saved);
    }


    // GET /api/accounts/{id} -> devuelve cuenta sin verificar ownership (IDOR)
    @GetMapping("/{id}")
    public ResponseEntity<AccountEntity> getById(@PathVariable("id") Long id) {
        Optional <AccountEntity> u = service.getById(id);
                return u.map
                (ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<AccountEntity> listByOwner(@RequestParam(name = "ownerId", required = false) Long ownerId) {
        if (ownerId == null) return service.listByOwnerId(null);
        return service.listByOwnerId(ownerId);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // log.error("Error deleting account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting account"));
        }
    }


    @PutMapping("/{id}/balance")
    public ResponseEntity<?> updateBalance(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> body) {
        if (body == null || !body.containsKey("newBalance")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "timestamp", System.currentTimeMillis(),
                    "status", 400,
                    "error", "Bad Request",
                    "message", "Missing field: newBalance"
            ));
        }

        try {
            BigDecimal newBalance = new BigDecimal(body.get("newBalance").toString());
            AccountEntity updated = service.updateBalance(id, newBalance);
            return ResponseEntity.ok(updated);

        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "timestamp", System.currentTimeMillis(),
                    "status", 400,
                    "error", "Bad Request",
                    "message", "Invalid newBalance value"
            ));
        }

    }
}
