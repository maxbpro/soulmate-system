package ru.maxb.soulmate.gateway.dto;

public record ProfileDto (
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
