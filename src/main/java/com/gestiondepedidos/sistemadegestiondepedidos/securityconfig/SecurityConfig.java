package com.gestiondepedidos.sistemadegestiondepedidos.securityconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desactivamos CSRF porque es una API REST (Stateless)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated() // Exigir token para TODAS las rutas (Guías y Transportistas)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {}) // Habilitar validación de tokens JWT
            );
            
        return http.build();
    }
}
