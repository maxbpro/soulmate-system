package ru.maxb.soulmate.gateway.service;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.gateway.config.KeycloakProperties;
import ru.maxb.soulmate.gateway.exception.ApiException;

@Service
@RequiredArgsConstructor
public class KeycloakApiTestService {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProperties;

    public UserRepresentation getUserRepresentation(String email) {
        var users = keycloak.realm(keycloakProperties.realm()).users().list();

        for (var user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }

        throw new ApiException("User not found by email=[ %s ]", email);
    }

    public void clear() {
        var users = keycloak.realm(keycloakProperties.realm()).users().list();
        for (var user : users) {
            if (!keycloakProperties.adminUsername().equals(user.getUsername()))    {
                keycloak.realm(keycloakProperties.realm()).users().delete(user.getId());
            }
        }
    }

}
