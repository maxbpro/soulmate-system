package ru.maxb.soulmate.gateway.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class AppTestConfig {

    @Autowired
    protected KeycloakProperties keycloakProperties;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakProperties.serverUrl())
            .realm(keycloakProperties.realm())
            .username(keycloakProperties.adminUsername())
            .password(keycloakProperties.adminPassword())
            .clientId(keycloakProperties.adminClientId())
            .clientSecret(keycloakProperties.clientSecret())
            .grantType(OAuth2Constants.PASSWORD)
            .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
