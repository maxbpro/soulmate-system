package ru.maxb.soulmate.common.event;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileCreatedDto(
        UUID id,
        String firstName,
        String lastName,
        String interestedIn,
        String gender,
        int ageMin,
        int ageMax,
        int radius,
        LocalDate birthDate,
        String landmarks
) {
}
