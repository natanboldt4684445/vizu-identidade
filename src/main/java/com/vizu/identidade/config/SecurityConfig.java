package com.vizu.identidade.config;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity h) throws Exception {
        return h.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(a -> a.requestMatchers("/auth/**", "/onboarding/**", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll().anyRequest().authenticated()).oauth2ResourceServer(o -> o.jwt(j -> {
        })).build();
    }

    @Bean
    JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String s) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(s.getBytes(StandardCharsets.UTF_8)));
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String s) {
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(s.getBytes(StandardCharsets.UTF_8), "HmacSHA256")).build();
    }
}
