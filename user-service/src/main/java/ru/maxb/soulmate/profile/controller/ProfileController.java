package ru.maxb.soulmate.profile.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.profile.service.ProfileService;
import ru.maxb.soulmate.user.api.ProfileApi;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationResponseDto;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class ProfileController implements ProfileApi {

    private final ProfileService profileService;

    @Override
    public ResponseEntity<ProfileRegistrationResponseDto> registration(@Valid ProfileRegistrationRequestDto profileRegistrationRequestDto) {
        var registered = profileService.register(profileRegistrationRequestDto);
        return ResponseEntity.ok(registered);
    }

    @Override
    public ResponseEntity<ProfileDto> findById(@NotNull UUID id) {
        ProfileDto profileDto = profileService.findById(id);
        return ResponseEntity.ok(profileDto);
    }

    @Override
    public ResponseEntity<ProfileRegistrationResponseDto> update(@NotNull UUID id, @Valid ProfileRegistrationRequestDto profileRegistrationRequestDto) {
        ProfileRegistrationResponseDto updated = profileService.update(id, profileRegistrationRequestDto);
        return ResponseEntity.ok(updated);
    }


    @Override
    public ResponseEntity<Void> compensateRegistration(@NotNull UUID id) {

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> delete(@NotNull UUID id) {
        return null;
    }



}
