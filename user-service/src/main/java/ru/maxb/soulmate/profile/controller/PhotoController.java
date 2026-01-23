package ru.maxb.soulmate.profile.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.profile.dto.PhotoObjectDto;
import ru.maxb.soulmate.profile.service.ProfileService;
import ru.maxb.soulmate.profile.util.SecurityUtils;
import ru.maxb.soulmate.user.api.PhotoApi;
import ru.maxb.soulmate.user.dto.UploadImage201Response;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PhotoController implements PhotoApi {

    private final ProfileService profileService;
    private final SecurityUtils securityUtils;


    @Override
    public ResponseEntity<UploadImage201Response> uploadImage(MultipartFile image) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        PhotoObjectDto photoObjectDto = profileService.uploadImage(currentUserId, image);
        return ResponseEntity.ok(new UploadImage201Response().
                id(photoObjectDto.id())
                .url(photoObjectDto.url()));
    }

    @Override
    public ResponseEntity<Void> deleteImage(@NotNull UUID photoId) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        profileService.deleteImage(currentUserId, photoId);
        return ResponseEntity.noContent().build();
    }

}
