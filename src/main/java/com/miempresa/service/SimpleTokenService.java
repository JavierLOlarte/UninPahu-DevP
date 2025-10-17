package com.miempresa.service;

import com.miempresa.entity.SimpleToken;
import com.miempresa.entity.UserEntity;

public interface SimpleTokenService {
    SimpleToken createTokenForUser(UserEntity user);
    SimpleToken findByToken(String token);
    void deleteByToken(String token);
}
