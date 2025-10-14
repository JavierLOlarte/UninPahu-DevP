package com.miempresa.service.Impl;


import com.miempresa.entity.UserEntity;
import com.miempresa.repository.UserRepository;
import com.miempresa.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // INTENCIONAL: uso de EntityManager para demostrar JPQL/SQL concatenado (vulnerable a inyección)
    @PersistenceContext
    private EntityManager em;

    @Override
    public UserEntity createUser(UserEntity user) {
        // INTENCIONAL: no hay validaciones ni hashing. Se permite que el request fije rol y password en texto plano.
        // Propósito educativo: demostrar creación insegura y exposición de credenciales.
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserEntity> getAllUsers() {
        // INTENCIONAL: endpoint público que devuelve todas las entidades (incluye password y relaciones EAGER).
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserEntity> getUserById(Long id) {
        // INTENCIONAL: no se valida ownership ni permisos.
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserEntity> searchByQuery(String q) {
        // INTENCIONAL: consulta construida por concatenación (vulnerable a inyección - SQL/JPQL).
        // Ejemplo intencionalmente inseguro: evita usar parámetros.
        String jpql = "SELECT u FROM UserEntity u WHERE u.nombre LIKE '%" + q + "%' OR u.email LIKE '%" + q + "%'";
        Query query = em.createQuery(jpql, UserEntity.class);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserEntity> login(String email, String password) {
        // INTENCIONAL: autenticación insegura:
        //  - Passwords en texto plano (almacenadas así en DB).
        //  - Sin throttling ni bloqueo por intentos.
        //  - Devuelve la entidad completa en caso de éxito.
        UserEntity user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }


    @Override
    public UserEntity updateUser(Long id, UserEntity patch) {
        // INTENCIONAL: actualización sin verificar que el requester sea el owner o admin.
        // Si patch contiene rol o password, se aplicará tal cual (escenario de escalado de privilegios).
        UserEntity existing = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (patch.getNombre() != null) existing.setNombre(patch.getNombre());
        if (patch.getEmail() != null) existing.setEmail(patch.getEmail());
        if (patch.getRol() != null) existing.setRol(patch.getRol()); // INTENCIONAL: permite cambio de rol desde request
        if (patch.getPassword() != null)
            existing.setPassword(patch.getPassword()); // INTENCIONAL: password en texto plano
        return userRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) {
        // INTENCIONAL: eliminación sin control de acceso ni soft-delete.
        userRepository.deleteById(id);


    }
}
