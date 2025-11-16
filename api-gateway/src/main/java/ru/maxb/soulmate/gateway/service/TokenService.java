package ru.maxb.soulmate.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.gateway.client.KeycloakClient;
import ru.maxb.soulmate.gateway.dto.TokenRefreshRequest;
import ru.maxb.soulmate.gateway.dto.TokenResponse;
import ru.maxb.soulmate.gateway.dto.UserLoginRequest;
import ru.maxb.soulmate.gateway.mapper.KeycloakMapper;
import ru.maxb.soulmate.gateway.mapper.TokenResponseMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final KeycloakClient keycloakClient;
    private final KeycloakMapper keycloakMapper;
    private final TokenResponseMapper tokenResponseMapper;

    public TokenResponse login(UserLoginRequest userLoginRequest) {
        var kcUserLoginRequest = keycloakMapper.toKeycloakUserLoginRequest(userLoginRequest);
        var tokenResponse = keycloakClient.login(kcUserLoginRequest);

        log.info("Token successfully generated for email = [{}]", userLoginRequest.getEmail());
        return tokenResponseMapper.toTokenResponse(tokenResponse);

        //log.error("Failed to generate token for email = [{}]", userLoginRequest.getEmail())
    }

    public TokenResponse refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        var kcTokenRefreshRequest = keycloakMapper.toKeycloakTokenRefreshRequest(tokenRefreshRequest);
        var tokenResponse = keycloakClient.refreshToken(kcTokenRefreshRequest);

        log.info("Token refreshed successfully");
        return tokenResponseMapper.toTokenResponse(tokenResponse);
    }
}














