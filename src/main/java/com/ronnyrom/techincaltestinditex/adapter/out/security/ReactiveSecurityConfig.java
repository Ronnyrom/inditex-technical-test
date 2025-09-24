package com.ronnyrom.techincaltestinditex.adapter.out.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class ReactiveSecurityConfig {

    private final ReactiveJwtAuthFilter reactiveJwtAuthFilter;

    public ReactiveSecurityConfig(ReactiveJwtAuthFilter reactiveJwtAuthFilter) {
        this.reactiveJwtAuthFilter = reactiveJwtAuthFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .headers(headers -> headers.disable())
                .authorizeExchange(ex -> ex
                        .pathMatchers("/h2-console/**").permitAll()
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/api/mgmt/1/assets/**").authenticated()
                        .anyExchange().permitAll()
                )
                .addFilterAt(reactiveJwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}