package com.miempresa.repository;

import com.miempresa.entity.SimpleToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimpleTokenRepository extends JpaRepository<SimpleToken, Long> {
    Optional<SimpleToken> findByToken(String token);
    void deleteByToken(String token);
}
