package ru.maxb.soulmate.profile.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.profile.model.OutboxType;
import ru.maxb.soulmate.profile.service.OutboxService;
import ru.maxb.soulmate.profile.service.ProfileService;
import ru.maxb.soulmate.user.api.ProfileApi;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;
import ru.maxb.soulmate.user.dto.ProfileUpdateRequestDto;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class ProfileController implements ProfileApi {

    private final ProfileService profileService;
    private final OutboxService outboxService;

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
    public ResponseEntity<ProfileDto> update(@NotNull UUID id, @Valid ProfileUpdateRequestDto profileUpdateRequestDto) {
        ProfileDto profileDto = profileService.update(id, profileUpdateRequestDto);
        return ResponseEntity.ok(profileDto);
    }

    @Override
    public ResponseEntity<Void> compensateRegistration(@NotNull UUID id) {
        profileService.hardDelete(id);
        outboxService.hardDelete(id, OutboxType.PROFILE_CREATED);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> delete(@NotNull UUID id) {
        profileService.softDelete(id);
        return null;
    }

    @Override
    public ResponseEntity<Void> uploadImage(@NotNull UUID id, MultipartFile file) {
        profileService.uploadImage(id, file);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteImage(@NotNull UUID id, @NotNull UUID photoId) {
        profileService.deleteImage(id, photoId);
        return ResponseEntity.ok().build();
    }
}
