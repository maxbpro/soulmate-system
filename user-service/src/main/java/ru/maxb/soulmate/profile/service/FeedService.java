package ru.maxb.soulmate.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.landmark.api.LandmarksApiClient;
import ru.maxb.soulmate.landmark.dto.LandmarkMatchDto;
import ru.maxb.soulmate.profile.exception.ProfileException;
import ru.maxb.soulmate.profile.mapper.ProfileMapper;
import ru.maxb.soulmate.profile.model.Gender;
import ru.maxb.soulmate.profile.model.ProfileEntity;
import ru.maxb.soulmate.profile.repository.ProfileRepository;
import ru.maxb.soulmate.user.dto.ProfileDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final ProfileRepository profileRepository;
    private final LandmarksApiClient landmarksApiClient;
    private final ProfileMapper profileMapper;

    public List<ProfileDto> getProfiles(UUID profileId, double lat, double lng, int page, int size) {

        //get profile preferences
        ProfileEntity profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileException("Profile not found, profileId=" + profileId));

        Gender interestedIn = profile.getInterestedIn();
        int ageMin = profile.getAgeMin();
        int ageMax = profile.getAgeMax();
        int radius = profile.getRadius();

        ResponseEntity<List<LandmarkMatchDto>> landmarkMatchesResponse = landmarksApiClient.getLandmarkMatches(lat, lng, radius,
                ageMin, ageMax, interestedIn.name(), page, size);

        List<LandmarkMatchDto> landmarkMatches = landmarkMatchesResponse.getBody();

        if(landmarkMatches == null || landmarkMatches.isEmpty()) {
            return new ArrayList<>();
        }

        return landmarkMatches.stream()
                .map(profileMapper::toProfileDto)
                .toList();
    }
}
