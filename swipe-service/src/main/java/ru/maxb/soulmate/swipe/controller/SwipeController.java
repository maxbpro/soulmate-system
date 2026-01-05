package ru.maxb.soulmate.swipe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.swipe.api.SwipeApi;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.exception.SwipeException;
import ru.maxb.soulmate.swipe.service.SwipeService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SwipeController implements SwipeApi {

    private final SwipeService swipeService;

    @Override
    public ResponseEntity<SwipeDto> createSwipe(@Valid SwipeRequestDto swipeRequestDto) {
        return ResponseEntity.status(201)
                .body(swipeService.createSwipe(getSub(), swipeRequestDto));

    }

    @Override
    public ResponseEntity<List<SwipeDto>> getSwipes() {
        return ResponseEntity.ok(swipeService.getSwipes(getSub()));
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
