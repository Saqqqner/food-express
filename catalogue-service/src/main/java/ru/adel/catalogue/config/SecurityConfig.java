package ru.adel.catalogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static ru.adel.catalogue.domain.constant.EndpointPermission.EDIT_CATALOGUE;
import static ru.adel.catalogue.domain.constant.EndpointPermission.VIEW_CATALOGUE;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers(HttpMethod.POST, "/catalogue-api/products")
                        .hasAuthority(EDIT_CATALOGUE.getAuthority())
                        .requestMatchers(HttpMethod.PATCH, "/catalogue-api/products/{productId:\\d}")
                        .hasAuthority(EDIT_CATALOGUE.getAuthority())
                        .requestMatchers(HttpMethod.DELETE, "/catalogue-api/products/{productId:\\d}")
                        .hasAuthority(EDIT_CATALOGUE.getAuthority())
                        .requestMatchers(HttpMethod.GET)
                        .hasAuthority(VIEW_CATALOGUE.getAuthority())
                        .anyRequest().denyAll())
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .jwt(Customizer.withDefaults()))
                .build();
    }
}
