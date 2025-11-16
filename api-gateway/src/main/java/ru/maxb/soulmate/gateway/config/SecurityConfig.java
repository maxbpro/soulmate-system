package ru.maxb.soulmate.gateway.config;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @SneakyThrows
    @Bean
    public SecurityFilterChain securityWebFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeExchange(exchangeSpec -> exchangeSpec
//                        //PUBLIC
//                        .pathMatchers(
//                                "/actuator/health",
//                                "/actuator/prometheus",
//                                "/actuator/info",
//                                "/v3/api-docs/**",
//                                "/swagger-ui.html",
//                                "/swagger-ui/**",
//                                "/v1/auth/registration",
//                                "/v1/auth/login",
//                                "/v1/auth/refresh-token"
//                        ).permitAll()
//                        //USER
//                        .pathMatchers("/v1/auth/me").hasAuthority("ROLE_individual.user")
//                        .anyExchange().authenticated()
//                )
                .oauth2ResourceServer(configurer ->
                        configurer.jwt(jwtConfigurer ->
                                jwtConfigurer.jwtAuthenticationConverter(keycloakAuthenticationConverter())))
                .build();
    }

    private Converter<Jwt, AbstractAuthenticationToken> keycloakAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return new KeycloakJwtAuthenticationConverter();
    }
}

















