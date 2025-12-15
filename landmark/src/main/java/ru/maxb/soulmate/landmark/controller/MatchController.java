package ru.maxb.soulmate.landmark.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.landmark.api.LandmarksApi;
import ru.maxb.soulmate.landmark.dto.LandmarkMatchDto;
import ru.maxb.soulmate.landmark.mapper.LandmarkMapper;
import ru.maxb.soulmate.landmark.model.Gender;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;
import ru.maxb.soulmate.landmark.service.LandmarkReadService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MatchController implements LandmarksApi {


    private final LandmarkReadService landmarkReadService;
    private final LandmarkMapper landmarkMapper;

    @Override
    public ResponseEntity<List<LandmarkMatchDto>> getLandmarkMatches(@NotNull @Valid Double lat,
                                                                     @NotNull @Valid Double lng,
                                                                     @NotNull @Valid Integer radius,
                                                                     @NotNull @Valid Integer ageMin,
                                                                     @NotNull @Valid Integer ageMax,
                                                                     @NotNull @Valid String interestedIn) {

        String profileId = null;
        List<LandmarkMatch> matches = landmarkReadService.findByParams(lat, lng, radius, ageMin, ageMax, Gender.valueOf(interestedIn),
                profileId);
        return ResponseEntity.ok(matches.stream()
                .map(landmarkMapper::toLandmarkMatchDto)
                .toList());
    }
}
