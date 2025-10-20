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

import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class ProfileController implements ProfileApi {

    private final ProfileService profileService;

    @Override
    public ResponseEntity<ProfileDto> registration(@Valid ProfileRegistrationRequestDto profileRegistrationRequestDto) {
        var registered = profileService.register(profileRegistrationRequestDto);
        return ResponseEntity.ok(registered);
    }

    @Override
    public ResponseEntity<ProfileDto> findById(@NotNull UUID id) {
        ProfileDto profileDto = profileService.findById(id);
        return ResponseEntity.ok(profileDto);
    }

    @Override
    public ResponseEntity<ProfileDto> update(@NotNull UUID id, @Valid ProfileRegistrationRequestDto profileRegistrationRequestDto) {
        ProfileDto profileDto = profileService.update(id, profileRegistrationRequestDto);
        return ResponseEntity.ok(profileDto);
    }


    @Override
    public ResponseEntity<Void> compensateRegistration(@NotNull UUID id) {
        profileService.hardDelete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> delete(@NotNull UUID id) {
        profileService.softDelete(id);
        return null;
    }


}
