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
import ru.maxb.soulmate.landmark.api.ProfileApi;
import ru.maxb.soulmate.landmark.exception.LandmarkException;
import ru.maxb.soulmate.landmark.service.ProfileService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProfileController implements ProfileApi {

    private final ProfileService profileService;

    @Override
    public ResponseEntity<Void> updateProfileLocation(@NotNull @Valid Double lat,
                                                      @NotNull @Valid Double lng) {
        profileService.updateLocation(getSub(), lat, lng);
        return ResponseEntity.ok().build();
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
