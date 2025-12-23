package ru.maxb.soulmate.swipe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.swipe.api.MatchApi;
import ru.maxb.soulmate.swipe.dto.MatchDto;
import ru.maxb.soulmate.swipe.exception.SwipeException;
import ru.maxb.soulmate.swipe.mapper.MatchMapper;
import ru.maxb.soulmate.swipe.service.MatchService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MatchController implements MatchApi {

    private final MatchService matchService;
    private final MatchMapper matchMapper;

    @Override
    public ResponseEntity<List<MatchDto>> match() {
        UUID profileId = getSub();
        return ResponseEntity.ok(matchService.getMatches(profileId).stream()
                .map(matchMapper::toDto)
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
                .orElseThrow(() -> new SwipeException("Not authenticated"));
    }
}
