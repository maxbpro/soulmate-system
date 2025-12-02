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

    //    @WithSpan("tokenService.login")
    public TokenResponse login(UserLoginRequest userLoginRequest) {
        try {
            var kcUserLoginRequest = keycloakMapper.toKeycloakUserLoginRequest(userLoginRequest);
            var login = keycloakClient.login(kcUserLoginRequest);
            log.info("Token successfully generated for email = [{}]", userLoginRequest.getEmail());
            return tokenResponseMapper.toTokenResponse(login);

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Failed to generate token for email = [{}]", userLoginRequest.getEmail());
        }

        return null;
    }

    public TokenResponse refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        try {
            var kcTokenRefreshRequest = keycloakMapper.toKeycloakTokenRefreshRequest(tokenRefreshRequest);
            var tokenResponse = keycloakClient.refreshToken(kcTokenRefreshRequest);
            log.info("Token refreshed successfully");
            return tokenResponseMapper.toTokenResponse(tokenResponse);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}














