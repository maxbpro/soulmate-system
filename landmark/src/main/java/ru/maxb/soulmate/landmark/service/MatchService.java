package ru.maxb.soulmate.landmark.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    private final int corePoolSize = Runtime.getRuntime().availableProcessors();
    private final int maxPoolSize = corePoolSize * 2;
    private final int queueCapacity = 1000;
    private final long keepAliveTime = 60L;

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            keepAliveTime,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueCapacity),
            new ThreadPoolExecutor.CallerRunsPolicy() // Important: handle queue overflow
    );


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

        int pageSize = 100;
        int currentPage = 0;
        long totalProfiles = profileReadService.getCount();

        log.debug("Starting matching for {} against {} existing profiles", profile.id(), totalProfiles);

        while ((long) currentPage * pageSize < totalProfiles) {
            int from = currentPage * pageSize;
            List<Profile> profiles = profileReadService.searchAll(from, pageSize);
            processProfiles(profiles, landmarks, profileEntity);
            currentPage++;

            if (currentPage % 10 == 0) {
                log.debug("Processed {}/{} profiles for {}",
                        Math.min((long) currentPage * pageSize, totalProfiles),
                        totalProfiles, profile.id());
            }
        }

        log.info("Completed matching process for {}", profile.id());
    }

    //todo Backpressure Control
    private void processProfiles(List<Profile> profiles, FaceResponseFacesInnerLandmark landmarks,
                                 Profile profileEntity) {
        List<Profile> filteredProfiles = profiles.stream()
                .filter(p -> !p.getProfileId().equals(profileEntity.getProfileId()))
                .toList();

        for (Profile otherProfile : filteredProfiles) {
            executor.submit(() -> {
                try {
                    processSingleProfile(otherProfile, landmarks, profileEntity);
                } catch (Exception e) {
                    log.error("Failed to compare profiles {} and {}: {}",
                            profileEntity.getProfileId(), otherProfile.getProfileId(),
                            e.getMessage());
                }
            });
        }
    }

    private void processSingleProfile(Profile otherProfile,
                                      FaceResponseFacesInnerLandmark landmarks,
                                      Profile profileEntity) throws JsonProcessingException {

        FaceResponseFacesInnerLandmark otherProfileLandmarks = objectMapper.readValue(
                otherProfile.getLandmarks(), FaceResponseFacesInnerLandmark.class);

        double distance = euclideanDistanceService.compare(landmarks, otherProfileLandmarks);

        if (distance < THRESHOLD) {
            saveLandmarkMatch(profileEntity, otherProfile, distance);
        }
    }

    private void saveLandmarkMatch(Profile profile1, Profile profile2, double distance) {
        Optional<Boolean> exists = landmarkMatchRepository.existsMatchBetweenProfiles(
                profile1.getProfileId(), profile2.getProfileId());

        if (exists.isEmpty() || !exists.get()) {
            var landmarkMatch = landmarkMapper.toLandmarkMatch(profile1, profile2, distance);
            landmarkMatchRepository.save(landmarkMatch);

            log.debug("Match found: {} <-> {} (distance: {})",
                    profile1.getProfileId(), profile2.getProfileId(), distance);
        } else {
            log.debug("Match already exists between {} and {}",
                    profile1.getProfileId(), profile2.getProfileId());
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
