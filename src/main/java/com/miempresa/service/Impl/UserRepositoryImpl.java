package com.miempresa.service.Impl;

import com.miempresa.entity.UserEntity;
import com.miempresa.service.UserRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public UserEntity findByEmailVuln(String email) {
        // === INTENCIONALMENTE VULNERABLE para laboratorio ===
        // Construir la SQL (concatenación deliberada)
        String sql = "SELECT id, email, nombre, password, rol FROM uninpahu.user WHERE email = '" + email + "'";
        // DEBUG: imprimir la SQL para ver exactamente la consulta que se ejecuta
        System.out.println("DEBUG - native sql: " + sql);

        try {
            @SuppressWarnings("unchecked")
            List<UserEntity> list = em.createNativeQuery(sql, UserEntity.class)
                    .getResultList(); // usar lista evita excepción por >1 resultado

            if (list == null || list.isEmpty()) {
                return null;
            }
            // devolver el primer resultado (para la demo de login)
            return list.get(0);
        } catch (Exception ex) {
            // log para depuración (no propagar para que la app no rompa)
            ex.printStackTrace();
            return null;
        }
    }
}
