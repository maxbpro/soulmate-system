package ru.maxb.soulmate.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.profile.exception.ProfileException;
import ru.maxb.soulmate.profile.mapper.ProfileMapper;
import ru.maxb.soulmate.profile.model.OutboxType;
import ru.maxb.soulmate.profile.model.ProfileEntity;
import ru.maxb.soulmate.profile.repository.ProfileRepository;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;

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


    @Transactional
    public ProfileDto register(ProfileRegistrationRequestDto requestDto) {
        String landmarksJson = getLandmarksJson(requestDto.getPhoto());
        log.info("IN - register: landmarks for profile: [{}] successfully received", requestDto.getEmail());
        var profileEntity = profileMapper.to(requestDto, landmarksJson);
        profileRepository.save(profileEntity);
        log.info("IN - register: profile: [{}] successfully registered", profileEntity.getEmail());

        saveToOutbox(profileEntity, OutboxType.PROFILE_CREATED);
        return profileMapper.from(profileEntity);
    }

    @Transactional
    public ProfileDto update(UUID id, ProfileRegistrationRequestDto requestDto) {
        var profileEntity = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", id));
        profileMapper.update(profileEntity, requestDto);
        profileRepository.save(profileEntity);

        saveToOutbox(profileEntity, OutboxType.PROFILE_UPDATED);
        return profileMapper.from(profileEntity);
    }

    private void saveToOutbox(ProfileEntity profileEntity, OutboxType outboxType) {
        String aggregationId = profileEntity.getId().toString();
        String aggregateType = ProfileEntity.class.getSimpleName();
        JsonNode payload = objectMapper.valueToTree(profileEntity);

        outboxService.save(aggregationId, aggregateType, payload, outboxType);
        log.info("IN - register: profile saved/updated in outbox: [{}]", profileEntity.getEmail());
    }

    private String getLandmarksJson(String photo) {
        try {
            var landmarks = faceLandmarkService.getLandmarks(photo);
            return objectMapper.writeValueAsString(landmarks);

        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage());
        }

        throw new ProfileException("Face API response has issues");
    }

    @Transactional(readOnly = true)
    public ProfileDto findById(UUID id) {
        var individual = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", id));
        log.info("IN - findById: profile with id = [{}] successfully found", id);
        return profileMapper.from(individual);
    }

    @Transactional
    public void softDelete(UUID id) {
        log.info("IN - softDelete: profile with id = [{}] successfully deleted", id);
        profileRepository.softDelete(id);
    }

    @Transactional
    public void hardDelete(UUID id) {
        var individual = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", id));
        log.info("IN - hardDelete: profile with id = [{}] successfully deleted", id);
        profileRepository.delete(individual);
    }

    @Transactional
    public void uploadImage(UUID id, MultipartFile file) {
        var profileEntity = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", id));

        String photoId = UUID.randomUUID().toString();
        profileEntity.getPhotos().add(photoId);
        objectStorageService.saveObject(photoId, file);
    }
}
