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
import ru.maxb.soulmate.landmark.util.SecurityUtils;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MatchController implements LandmarksApi {


    private final LandmarkReadService landmarkReadService;
    private final LandmarkMapper landmarkMapper;
    private final SecurityUtils securityUtils;

    @Override
    public ResponseEntity<List<LandmarkMatchDto>> getLandmarkMatches(Double lat,
                                                                     Double lng,
                                                                     Integer radius,
                                                                     Integer ageMin,
                                                                     Integer ageMax,
                                                                     String interestedIn,
                                                                     Integer page,
                                                                     Integer pageNumber) {
        List<LandmarkMatch> matches = landmarkReadService.findByParams(lat, lng, radius, ageMin, ageMax, Gender.valueOf(interestedIn),
                page, pageNumber, securityUtils.getCurrentUserId());
        return ResponseEntity.ok(matches.stream()
                .map(landmarkMapper::toLandmarkMatchDto)
                .toList());
    }
}
