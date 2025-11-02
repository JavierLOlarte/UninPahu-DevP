package com.miempresa.service;

import com.miempresa.entity.UserEntity;

public interface UserRepositoryCustom {

    UserEntity findByEmailVuln(String email);
}
