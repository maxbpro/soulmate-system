package ru.maxb.soulmate.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.gateway.client.KeycloakClient;
import ru.maxb.soulmate.gateway.dto.*;
import ru.maxb.soulmate.gateway.exception.ApiException;
import ru.maxb.soulmate.gateway.mapper.TokenResponseMapper;
import ru.maxb.soulmate.keycloak.dto.UserLoginRequest;

import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final ProfileService profileService;
    private final KeycloakClient keycloakClient;
    private final TokenResponseMapper tokenResponseMapper;

    public UserInfoResponse getUserInfo() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(UserService::getUserInfoResponse)
                .orElseThrow(() -> new ApiException("No authentication present"));
    }

    private static UserInfoResponse getUserInfoResponse(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            var userInfoResponse = new UserInfoResponse();
            userInfoResponse.setId(jwt.getSubject());
            userInfoResponse.setEmail(jwt.getClaimAsString("email"));
            userInfoResponse.setRoles(jwt.getClaimAsStringList("roles"));

            if (jwt.getIssuedAt() != null) {
                userInfoResponse.setCreatedAt(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));
            }
            log.info("User[email={}] was successfully get info", jwt.getClaimAsString("email"));

            return userInfoResponse;
        }

        log.error("Can not get current user info: Invalid principal");
        throw new ApiException("Can not get current user info: Invalid principal");
    }

    //    @WithSpan("userService.register")
    public TokenResponse register(MultipartFile multipartFile, GatewayRegistrationRequestDto request) {
        GatewayRegistrationResponseDto responseDto = profileService.register(multipartFile, request);

        UUID personId = responseDto.getId();

        ru.maxb.soulmate.keycloak.dto.TokenResponse adminToken = keycloakClient.adminLogin();

        var kcUser = new KeycloakUserRepresentation(
                null,
                request.getEmail(),
                request.getEmail(),
                true,
                true,
                null
        );

        String kcUserId = keycloakClient.registerUser(adminToken, kcUser);

        var cred = new KeycloakCredentialsRepresentation(
                "password",
                request.getPassword(),
                false
        );

        keycloakClient.resetUserPassword(kcUserId, cred, adminToken.getAccessToken());

        ru.maxb.soulmate.keycloak.dto.TokenResponse response = keycloakClient.login(
                new UserLoginRequest(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        profileService.compensateRegistration(personId.toString());

        return tokenResponseMapper.toTokenResponse(response);
    }
}
