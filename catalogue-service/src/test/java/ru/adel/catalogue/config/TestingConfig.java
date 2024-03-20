package ru.adel.catalogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.Mockito.mock;

@Configuration
public class TestingConfig {

    @Bean
    public JwtDecoder decoder() {
        return mock(JwtDecoder.class);
    }
}
