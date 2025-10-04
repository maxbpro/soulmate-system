package ru.maxb.soulmate.gateway.dto;

public record KeycloakCredentialsRepresentation(
        String type,
        String value,
        Boolean temporary
) {
}
