package ru.maxb.soulmate.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.profile.exception.ProfileException;
import ru.maxb.soulmate.profile.mapper.ProfileMapper;
import ru.maxb.soulmate.profile.model.OutboxEntity;
import ru.maxb.soulmate.profile.model.ProfileEntity;
import ru.maxb.soulmate.profile.repository.OutboxRepository;
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
    private final OutboxRepository outboxRepository;


    @Transactional
    public ProfileDto register(ProfileRegistrationRequestDto requestDto) {
        String landmarksJson = getLandmarksJson(requestDto.getPhoto());
        log.info("IN - register: landmarks for profile: [{}] successfully received", requestDto.getEmail());
        var profileEntity = profileMapper.to(requestDto, landmarksJson);
        profileRepository.save(profileEntity);
        log.info("IN - register: profile: [{}] successfully registered", profileEntity.getEmail());


        //out box
        OutboxEntity outboxEntity = new OutboxEntity();
        outboxEntity.setAggregateType(ProfileEntity.class.getSimpleName());
        outboxEntity.setAggregateId(profileEntity.getId().toString());
        outboxEntity.setType("Profile created");
        outboxEntity.setPayload(objectMapper.valueToTree(profileEntity));
        outboxRepository.save(outboxEntity);

        return profileMapper.from(profileEntity);
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
    public ProfileDto update(UUID id, ProfileRegistrationRequestDto requestDto) {
        var profileEntity = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", id));
        profileMapper.update(profileEntity, requestDto);
        profileRepository.save(profileEntity);
        return profileMapper.from(profileEntity);
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
