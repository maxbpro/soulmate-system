package ru.maxb.soulmate.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.profile.dto.PhotoObjectDto;
import ru.maxb.soulmate.profile.exception.FaceRecognitionException;
import ru.maxb.soulmate.profile.exception.ProfileException;
import ru.maxb.soulmate.profile.mapper.ProfileMapper;
import ru.maxb.soulmate.profile.model.OutboxType;
import ru.maxb.soulmate.profile.model.ProfileEntity;
import ru.maxb.soulmate.profile.repository.ProfileRepository;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;
import ru.maxb.soulmate.user.dto.ProfileUpdateRequestDto;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final ObjectStorageService objectStorageService;
    private final FaceLandmarkService faceLandmarkService;
    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;

    @WithSpan
    @Transactional
    public ProfileDto register(ProfileRegistrationRequestDto requestDto) {
        String landmarksJson = getLandmarksJson(requestDto.getPhoto());
        log.info("Extracted landmarks for profile: {}", requestDto.getEmail());

        if (profileRepository.findById(requestDto.getPrincipalId()).isPresent()) {
            log.error("Profile with id {} already exists", requestDto.getPrincipalId());
            throw new ProfileException("PrincipalId already registered: " + requestDto.getEmail());
        }

        var profileEntity = profileMapper.to(requestDto, landmarksJson);
        profileRepository.save(profileEntity);
        log.info("Profile registered successfully: {}", profileEntity.getId());

        saveToOutbox(profileEntity, OutboxType.PROFILE_CREATED);

        return profileMapper.from(profileEntity);
    }

    @WithSpan
    @Transactional
    public ProfileDto update(UUID id, ProfileUpdateRequestDto requestDto) {
        var profileEntity = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", id));

        profileMapper.update(profileEntity, requestDto);
        log.info("Profile updated: {}", id);

        profileRepository.save(profileEntity);

        saveToOutbox(profileEntity, OutboxType.PROFILE_UPDATED);
        return profileMapper.from(profileEntity);
    }


    @Transactional(readOnly = true)
    public ProfileDto findById(UUID id) {
        var profile = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", id));
        log.debug("Profile retrieved: {}", id);
        return profileMapper.from(profile);
    }

    @WithSpan
    @Transactional
    public void softDelete(UUID id) {
        var profileEntity = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", id));

        profileRepository.softDelete(id);
        log.info("Profile soft deleted: {}", id);

        saveToOutbox(profileEntity, OutboxType.PROFILE_DELETED);
    }

    @WithSpan
    @Transactional
    public void hardDelete(UUID id) {
        var profile = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", id));
        profileRepository.delete(profile);
        log.warn("Profile hard deleted: {}", id);
    }

    @WithSpan
    @Transactional
    public PhotoObjectDto uploadImage(UUID profileId, MultipartFile file) {
        var profileEntity = profileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", profileId));

        UUID photoId = UUID.randomUUID();
        PhotoObjectDto photoObjectDto = objectStorageService.saveObject(profileId, photoId, file);
        profileEntity.getPhotos().add(String.valueOf(photoId));
        profileRepository.save(profileEntity);

        log.info("Photo uploaded: {} for profile: {} with url {}", photoId, profileId, photoObjectDto.url());
        return photoObjectDto;
    }

    @WithSpan
    @Transactional
    public void deleteImage(UUID profileId, UUID photoId) {
        var profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", profileId));

        if (!profile.getPhotos().contains(String.valueOf(photoId))) {
            throw new ProfileException("Profile doesn't have photo with id=[%s]", photoId);
        }

        objectStorageService.deleteObject(profileId, photoId);

        profile.getPhotos().removeIf(v -> v.equals(String.valueOf(photoId)));
        profileRepository.save(profile);

        log.info("Photo deleted: {} from profile: {}", photoId, profileId);
    }

    private void saveToOutbox(ProfileEntity profileEntity, OutboxType outboxType) {
        try {
            String aggregationId = profileEntity.getId().toString();
            String aggregateType = ProfileEntity.class.getSimpleName();
            JsonNode payload = objectMapper.valueToTree(profileEntity);

            outboxService.save(aggregationId, aggregateType, payload, outboxType);
            log.debug("Outbox event saved for profile: {} - {}", profileEntity.getId(), outboxType);

        } catch (Exception e) {
            log.error("Failed to save to outbox for profile: {}", profileEntity.getId(), e);
            throw new ProfileException("Failed to save to outbox for profile: " + profileEntity.getId(), e);
        }
    }

    private String getLandmarksJson(String photo) {
        try {
            var landmarks = faceLandmarkService.getLandmarks(photo);
            return objectMapper.writeValueAsString(landmarks);

        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize landmarks: {}", ex.getMessage());
            throw new FaceRecognitionException("Failed to process face landmarks", ex);
        } catch (Exception ex) {
            log.error("Face landmark extraction failed: {}", ex.getMessage());
            throw new FaceRecognitionException("Face API request failed", ex);
        }
    }
}
