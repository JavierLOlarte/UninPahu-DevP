package com.miempresa.repository;

import com.miempresa.entity.AccountEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepositoryVuln extends JpaRepository<AccountEntity, Long> {
    // Busca por la propiedad usuario.id correctamente
    List<AccountEntity> findByUsuario_Id(Long usuarioId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM uninpahu.account WHERE id = :id", nativeQuery = true)
    void deleteNative(@Param("id") Long id);

}
