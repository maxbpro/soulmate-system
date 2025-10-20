package ru.maxb.soulmate.profile.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.profile.exception.ProfileException;
import ru.maxb.soulmate.profile.mapper.ProfileMapper;
import ru.maxb.soulmate.profile.repository.ProfileRepository;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationResponseDto;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    @Transactional
    public ProfileRegistrationResponseDto register(ProfileRegistrationRequestDto requestDto) {
        var profileEntity = profileMapper.to(requestDto);
        profileRepository.save(profileEntity);
        log.info("IN - register: profile: [{}] successfully registered", profileEntity.getEmail());
        return new ProfileRegistrationResponseDto(profileEntity.getId().toString());
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
    public ProfileRegistrationResponseDto update(UUID id, ProfileRegistrationRequestDto requestDto) {
        var profileEntity = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileException("Profile not found by id=[%s]", id));
        profileMapper.update(profileEntity, requestDto);
        profileRepository.save(profileEntity);
        return new ProfileRegistrationResponseDto(profileEntity.getId().toString());
    }
}
