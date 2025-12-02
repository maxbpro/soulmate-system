package ru.maxb.soulmate.profile.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.profile.exception.ProfileException;
import ru.maxb.soulmate.profile.mapper.ProfileMapper;
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

    @Transactional
    public ProfileDto register(ProfileRegistrationRequestDto requestDto) {
        var profileEntity = profileMapper.to(requestDto);
        profileRepository.save(profileEntity);
        log.info("IN - register: profile: [{}] successfully registered", profileEntity.getEmail());
        return profileMapper.from(profileEntity);
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
