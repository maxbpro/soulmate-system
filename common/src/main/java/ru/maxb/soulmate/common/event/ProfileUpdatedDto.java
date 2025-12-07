package ru.maxb.soulmate.common.event;

public record ProfileUpdatedDto(
        String firstName,
        String lastName,
        String interestedIn,
        String gender,
        int ageMin,
        int ageMax,
        int radius
) {
}
