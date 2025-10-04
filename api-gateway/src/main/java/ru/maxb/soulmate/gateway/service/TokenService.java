package ru.maxb.soulmate.gateway.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.maxb.soulmate.gateway.client.KeycloakClient;
import ru.maxb.soulmate.gateway.mapper.KeycloakMapper;
import ru.maxb.soulmate.gateway.mapper.TokenResponseMapper;
import ru.maxb.soulmate.profile.dto.TokenRefreshRequest;
import ru.maxb.soulmate.profile.dto.TokenResponse;
import ru.maxb.soulmate.profile.dto.UserLoginRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final KeycloakClient keycloakClient;
    private final KeycloakMapper keycloakMapper;
    private final TokenResponseMapper tokenResponseMapper;

    @WithSpan("tokenService.login")
    public Mono<TokenResponse> login(UserLoginRequest userLoginRequest) {
        var kcUserLoginRequest = keycloakMapper.toKeycloakUserLoginRequest(userLoginRequest);
        return keycloakClient.login(kcUserLoginRequest)
                .doOnNext(t -> log.info("Token successfully generated for email = [{}]", userLoginRequest.getEmail()))
                .doOnError(e -> log.error("Failed to generate token for email = [{}]", userLoginRequest.getEmail()))
                .map(tokenResponseMapper::toTokenResponse);
    }

    public Mono<TokenResponse> refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        var kcTokenRefreshRequest = keycloakMapper.toKeycloakTokenRefreshRequest(tokenRefreshRequest);
        return keycloakClient.refreshToken(kcTokenRefreshRequest)
                .doOnNext(r -> log.info("Token refreshed successfully"))
                .map(tokenResponseMapper::toTokenResponse);
    }
}














