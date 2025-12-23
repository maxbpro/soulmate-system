package ru.maxb.soulmate.landmark.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.landmark.api.LandmarksApi;
import ru.maxb.soulmate.landmark.dto.LandmarkMatchDto;
import ru.maxb.soulmate.landmark.exception.LandmarkException;
import ru.maxb.soulmate.landmark.mapper.LandmarkMapper;
import ru.maxb.soulmate.landmark.model.Gender;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;
import ru.maxb.soulmate.landmark.service.LandmarkReadService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        UUID profileId = getSub();
        List<LandmarkMatch> matches = landmarkReadService.findByParams(lat, lng, radius, ageMin, ageMax, Gender.valueOf(interestedIn),
                profileId);
        return ResponseEntity.ok(matches.stream()
                .map(landmarkMapper::toLandmarkMatchDto)
                .toList());
    }

    private UUID getSub() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                .map(authentication -> (JwtAuthenticationToken) authentication)
                .map(JwtAuthenticationToken::getToken)
                .map(Jwt::getClaims)
                .filter(v -> v.containsKey("sub"))
                .map(v -> (String) v.get("sub"))
                .map(UUID::fromString)
                .orElseThrow(() -> new LandmarkException("Not authenticated"));
    }
}
