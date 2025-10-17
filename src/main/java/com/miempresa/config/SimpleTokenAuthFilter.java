package com.miempresa.config;

import com.miempresa.entity.SimpleToken;
import com.miempresa.entity.UserEntity;
import com.miempresa.service.SimpleTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro deliberadamente vulnerable: acepta "Authorization: Bearer <token>".
 * - Busca token en DB mediante SimpleTokenService.findByToken(...)
 * - Si existe, crea un Authentication simple y lo pone en SecurityContext
 * - No hay comprobación de roles/propiedad (IDOR demo)
 */
@Component
@RequiredArgsConstructor
public class SimpleTokenAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SimpleTokenAuthFilter.class);

    private final SimpleTokenService tokenService;
    private final String headerName = "Authorization"; // "Authorization: Bearer <token>"

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String header = request.getHeader(headerName);

            // <-- PRIMER LOG: muestra el header (útil para comprobar si el front envía Authorization)
            logger.info("Auth header: {}", header);

            if (header != null && header.startsWith("Bearer ")) {
                String tokenValue = header.substring(7).trim();
                if (!tokenValue.isEmpty()) {
                    SimpleToken st = tokenService.findByToken(tokenValue);

                    // <-- SEGUNDO LOG: indica si el token se resolvió en la BD
                    logger.info("Token found in DB: {}", st != null);

                    // <-- TERCER LOG: si existe, imprime el email del usuario asociado
                    if (st != null && st.getUser() != null) {
                        logger.info("Token belongs to user: {}", st.getUser().getEmail());
                    }

                    if (st != null && st.getUser() != null) {
                        UserEntity user = st.getUser();

                        // CREAMOS Authentication simple y lo colocamos en el contexto de seguridad.
                        // NOTA: aquí no asignamos roles ni verificamos ownership (intencionalmente vulnerable).
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        } catch (Exception ex) {
            // No queremos tirar exception aquí: si algo falla en el filtro, dejamos que la cadena continúe
            // y Spring devolverá 401/403 según corresponda.
            logger.error("Error en SimpleTokenAuthFilter: " + ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
