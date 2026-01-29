package ru.maxb.soulmate.swipe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ResourceServerSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health",
                                "/actuator/health/**",
                                "/actuator/prometheus",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll() // Permit access to public paths

                        .requestMatchers(
                                "/api/v1/matches",
                                "/api/v1/swipes"
                        ).hasAuthority("ROLE_soulmateapp.user")
                        .anyRequest()
                        .authenticated()
                ).exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden");
                        })
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtAuthenticationConverter());
        return jwtAuthenticationConverter;
    }
}
