package com.miempresa.repository;

import com.miempresa.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepositoryVuln extends JpaRepository<AccountEntity, Long> {
    // Busca por la propiedad usuario.id correctamente
    List<AccountEntity> findByUsuario_Id(Long usuarioId);
}
