package com.miempresa.repository;

import com.miempresa.entity.UserEntity;
import com.miempresa.service.UserRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> , UserRepositoryCustom {


    // INTENCIONAL: no se protege la búsqueda por email, puede usarse para enumeración de usuarios
    UserEntity findByEmail(String email);

}
