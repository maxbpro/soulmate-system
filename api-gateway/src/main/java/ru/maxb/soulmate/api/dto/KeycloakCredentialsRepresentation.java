package ru.maxb.soulmate.api.dto;

public record KeycloakCredentialsRepresentation(
        String type,
        String value,
        Boolean temporary
) {
}
