package ru.maxb.soulmate.landmark.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.maxb.soulmate.common.event.ProfileCreatedDto;
import ru.maxb.soulmate.landmark.model.Profile;
import ru.maxb.soulmate.face.dto.FaceResponseFacesInnerLandmark;
import ru.maxb.soulmate.landmark.mapper.LandmarkMapper;
import ru.maxb.soulmate.landmark.mapper.ProfileMapper;
import ru.maxb.soulmate.landmark.repository.LandmarkMatchRepository;
import ru.maxb.soulmate.landmark.repository.ProfileRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final LandmarkMatchRepository landmarkMatchRepository;
    private final ProfileRepository profileRepository;

    private final ProfileMapper profileMapper;
    private final LandmarkMapper landmarkMapper;

    private final EuclideanDistanceService euclideanDistanceService;
    private final ObjectMapper objectMapper;
    private final ProfileReadService profileReadService;
    private final ProfileService profileService;

    private final double THRESHOLD = 0.25;

    public void updateProfileRecord(ProfileCreatedDto profileCreatedDto) throws JsonProcessingException {
        Optional<Profile> profileOptional = profileReadService.findById(profileCreatedDto.id());

        if (profileOptional.isEmpty()) {
            log.info("Starting matching process for {}", profileCreatedDto.id());
            startMatchingProcess(profileCreatedDto);
        } else {
            log.info("Updating profile {}", profileCreatedDto.id());
            profileService.update(profileOptional.get(), profileCreatedDto);
        }
    }


    private void startMatchingProcess(ProfileCreatedDto profile) throws JsonProcessingException {
        var profileEntity = profileMapper.toProfile(profile);
        profileRepository.save(profileEntity);
        log.info("New profile {} saved", profileEntity.getProfileId());

        FaceResponseFacesInnerLandmark landmarks = objectMapper.readValue(profile.landmarks(),
                FaceResponseFacesInnerLandmark.class);

        int pageSize = 10;
        int currentPage = 0;

        long count = profileReadService.getCount();
        int totalPages = (int) Math.ceil((double) count / pageSize);

        while (currentPage < totalPages) {
            int from = currentPage * pageSize;
            List<Profile> profiles = profileReadService.searchAll(from, pageSize);
            processProfiles(profiles, landmarks, profileEntity);
            currentPage++;
        }
    }

    private void processProfiles(List<Profile> profiles, FaceResponseFacesInnerLandmark landmarks,
                                 Profile profileEntity) {
        for (Profile otherProfile : profiles) {
            if (otherProfile.getProfileId().equals(profileEntity.getProfileId())) {
                continue;
            }

            try {
                FaceResponseFacesInnerLandmark otherProfileLandmarks = objectMapper.readValue(otherProfile.getLandmarks(), FaceResponseFacesInnerLandmark.class);
                double compared = euclideanDistanceService.compare(landmarks, otherProfileLandmarks);

                if (compared < THRESHOLD) {
                    var landmarkMatch = landmarkMapper.toLandmarkMatch(profileEntity, otherProfile);
                    landmarkMatchRepository.save(landmarkMatch);
                    log.info("New LandmarkMatch saved between {} and {}", profileEntity.getProfileId(), otherProfile.getProfileId());
                }

            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }
    }
}
