package ru.maxb.soulmate.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.gateway.client.KeycloakClient;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationResponseDto;
import ru.maxb.soulmate.gateway.dto.KeycloakCredentialsRepresentation;
import ru.maxb.soulmate.gateway.dto.KeycloakUserRepresentation;
import ru.maxb.soulmate.gateway.dto.TokenResponse;
import ru.maxb.soulmate.gateway.dto.UserInfoResponse;
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
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(UserService::getUserInfoResponseMono)
                .orElseThrow(() -> new ApiException("No authentication present"));
    }

    private static UserInfoResponse getUserInfoResponseMono(Authentication authentication) {
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
    public TokenResponse register(GatewayRegistrationRequestDto request) {
        GatewayRegistrationResponseDto registrationResponseDto = profileService.register(request);
        UUID personId = registrationResponseDto.getId();

        ru.maxb.soulmate.keycloak.dto.TokenResponse adminTokenResponse = keycloakClient.adminLogin();

        var kcUser = new KeycloakUserRepresentation(
                null,
                request.getEmail(),
                request.getEmail(),
                true,
                true,
                null
        );

        try{

            String kcUserId = keycloakClient.registerUser(adminTokenResponse, kcUser);

            var cred = new KeycloakCredentialsRepresentation(
                    "password",
                    request.getPassword(),
                    false
            );

            keycloakClient.resetUserPassword(kcUserId, cred, adminTokenResponse.getAccessToken());

            ru.maxb.soulmate.keycloak.dto.TokenResponse tokenResponse = keycloakClient.login(
                    new UserLoginRequest(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            return tokenResponseMapper.toTokenResponse(tokenResponse);


        }catch (Exception ex){
            ex.printStackTrace();
            profileService.compensateRegistration(personId.toString());
        }

        return null;
    }
}
