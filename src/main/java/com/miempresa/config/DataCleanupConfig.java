package com.miempresa.config;

import com.miempresa.repository.SimpleTokenRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataCleanupConfig {

    private final SimpleTokenRepository tokenRepository;

    public DataCleanupConfig(SimpleTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Bean
    public CommandLineRunner purgeTokensOnStartup() {
        return args -> {
            tokenRepository.deleteAll();
            System.out.println("✅ simple_token table purged on startup");
        };
    }
}
