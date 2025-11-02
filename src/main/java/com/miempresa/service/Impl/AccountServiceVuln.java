package com.miempresa.service.Impl;

import com.miempresa.entity.AccountEntity;
import com.miempresa.repository.AccountRepositoryVuln;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AccountServiceVuln {

    private final AccountRepositoryVuln accountRepository;

    // Crear cuenta (vulnerabilidad: se permite asignar usuario arbitrario)
    public AccountEntity createAccount(AccountEntity account) {
        return accountRepository.save(account);
    }

    // Obtener cuenta por ID (sin validación de ownership)
    public Optional<AccountEntity> getById(Long id) {
        return accountRepository.findById(id);
    }

    // Listar cuentas por usuarioId (si es null devuelve todas)
    public List<AccountEntity> listByOwnerId(Long ownerId) {
        if (ownerId == null) {
            return accountRepository.findAll();
        }
        return accountRepository.findByUsuario_Id(ownerId);
    }

    // Eliminar cuenta sin validar propietario

    @Transactional
    public void delete(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new IllegalArgumentException("Account not found: " + id);
        }
        AccountEntity acc = accountRepository.findById(id).orElse(null);
        log.info("FOUND BEFORE DELETE: {}", acc);

        accountRepository.deleteNative(id);

        log.info("Deleted (native)");
    }


    // Actualizar monto (sin validaciones ni seguridad)
    public AccountEntity updateBalance(Long id, BigDecimal newBalance) {
        Optional<AccountEntity> optAcc = accountRepository.findById(id);
        if (optAcc.isEmpty()) {
            throw new RuntimeException("Account not found");
        }
        AccountEntity account = optAcc.get();
        // usa el nombre de campo real de tu entidad
        account.setMonto(newBalance);
        return accountRepository.save(account);
    }
}
