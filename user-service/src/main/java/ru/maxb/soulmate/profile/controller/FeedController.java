package ru.maxb.soulmate.profile.controller;

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
import ru.maxb.soulmate.profile.exception.ProfileException;
import ru.maxb.soulmate.profile.service.FeedService;
import ru.maxb.soulmate.user.api.FeedApi;
import ru.maxb.soulmate.user.dto.ProfileDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedController implements FeedApi {

    private final FeedService feedService;

    @Override
    public ResponseEntity<List<ProfileDto>> getFeed(@NotNull @Valid Double lat,
                                                    @NotNull @Valid Double lng) {
        UUID profileId = getSub();
        return ResponseEntity.ok(feedService.getProfiles(profileId, lat, lng));
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
                .orElseThrow(() -> new ProfileException("Not authenticated"));
    }
}
