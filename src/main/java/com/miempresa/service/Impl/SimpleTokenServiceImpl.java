package com.miempresa.service.Impl;

import com.miempresa.entity.SimpleToken;
import com.miempresa.entity.UserEntity;
import com.miempresa.repository.SimpleTokenRepository;
import com.miempresa.service.SimpleTokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SimpleTokenServiceImpl implements SimpleTokenService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleTokenServiceImpl.class);

    private final SimpleTokenRepository tokenRepository;

    /**
     * Crea y persiste un SimpleToken ligado al usuario.
     * Implementación intencionalmente vulnerable: token predecible "TOKEN-"+email
     */
    @Override
    public SimpleToken createTokenForUser(UserEntity user) {
        if (user == null) throw new IllegalArgumentException("user must not be null");

        // Token predecible para el laboratorio (intencional)
        String tokenValue = "TOKEN-" + user.getEmail();

        // 🔹 Borrar token antiguo si existe
        SimpleToken existing = tokenRepository.findByToken(tokenValue).orElse(null);
        if (existing != null) {
            tokenRepository.delete(existing);
            logger.info("SimpleToken antiguo borrado: {}", tokenValue);
        }

        // Crear y guardar nuevo token
        SimpleToken t = SimpleToken.builder()
                .token(tokenValue)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        SimpleToken saved = tokenRepository.save(t);
        logger.info("SimpleToken creado para user={} token={}", user.getEmail(), saved.getToken());
        return saved;
    }


    /**
     * Busca token por su valor. Devuelve null si no existe.
     */
    @Override
    public SimpleToken findByToken(String token) {
        if (token == null || token.isBlank()) return null;
        return tokenRepository.findByToken(token).orElse(null);
    }

    /**
     * Borra token por su valor (logout).
     */
    @Override
    public void deleteByToken(String token) {
        if (token == null || token.isBlank()) return;
        tokenRepository.deleteByToken(token);
        logger.info("SimpleToken borrado: {}", token);
    }
}
