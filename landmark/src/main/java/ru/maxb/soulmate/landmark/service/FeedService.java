package ru.maxb.soulmate.landmark.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.landmark.model.Gender;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final LandmarkReadService landmarkReadService;

    public List<LandmarkMatch> getLandmarkMatches(double latitude, double longitude, int radius,
                                                  int ageMin, int ageMax,
                                                  String interestedIn, String excludeProfileId) {

        return landmarkReadService.findByParams(latitude, longitude, radius, ageMin, ageMax,
                Gender.valueOf(interestedIn), excludeProfileId);
    }
}
