package ru.maxb.soulmate.landmark.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.common.event.ProfileCreatedDto;
import ru.maxb.soulmate.landmark.exception.LandmarkException;
import ru.maxb.soulmate.landmark.mapper.ProfileMapper;
import ru.maxb.soulmate.landmark.model.Profile;
import ru.maxb.soulmate.landmark.repository.ProfileRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileMapper profileMapper;
    private final ProfileRepository profileRepository;
    private final ProfileReadService profileReadService;

    public Profile updateLocation(UUID profileId, double lat, double lng) {
        Profile profile = profileReadService.findById(profileId)
                .orElseThrow(() -> new LandmarkException("Profile not found by id: " + profileId));
        profileMapper.updateLocation(profile, new GeoPoint(lat, lng));
        return profileRepository.save(profile);
    }

    public Profile update(Profile profile, ProfileCreatedDto profileCreatedDto) {
        profileMapper.update(profile, profileCreatedDto);
        return profileRepository.save(profile);
    }
}
