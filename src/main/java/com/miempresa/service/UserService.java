package com.miempresa.service;

import com.miempresa.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserEntity createUser(UserEntity user);

    List<UserEntity> getAllUsers();

    Optional<UserEntity> getUserById(Long id);

    /**
     * Búsqueda vulnerable construida por concatenación para fines educativos.
     */
    List<UserEntity> searchByQuery(String q);

    /**
     * Login inseguro: compara passwords en texto plano.
     * Devuelve el UserEntity completo en caso de éxito.
     */
    Optional<UserEntity> login(String email, String password);

    UserEntity updateUser(Long id, UserEntity patch);

    void deleteUser(Long id);

}
