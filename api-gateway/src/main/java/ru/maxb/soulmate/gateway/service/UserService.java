package ru.maxb.soulmate.gateway.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.maxb.soulmate.gateway.client.KeycloakClient;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.KeycloakCredentialsRepresentation;
import ru.maxb.soulmate.gateway.dto.KeycloakUserRepresentation;
import ru.maxb.soulmate.gateway.dto.TokenResponse;
import ru.maxb.soulmate.gateway.dto.UserInfoResponse;
import ru.maxb.soulmate.gateway.exception.ApiException;
import ru.maxb.soulmate.gateway.mapper.TokenResponseMapper;
import ru.maxb.soulmate.keycloak.dto.UserLoginRequest;

import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final ProfileService profileService;
    private final KeycloakClient keycloakClient;
    private final TokenResponseMapper tokenResponseMapper;

    @WithSpan("userService.getUserInfo")
    public Mono<UserInfoResponse> getUserInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(UserService::getUserInfoResponseMono)
                .switchIfEmpty(Mono.error(new ApiException("No authentication present")));
    }

    private static Mono<UserInfoResponse> getUserInfoResponseMono(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            var userInfoResponse = new UserInfoResponse();
            userInfoResponse.setId(jwt.getSubject());
            userInfoResponse.setEmail(jwt.getClaimAsString("email"));
            userInfoResponse.setRoles(jwt.getClaimAsStringList("roles"));

            if (jwt.getIssuedAt() != null) {
                userInfoResponse.setCreatedAt(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));
            }
            log.info("User[email={}] was successfully get info", jwt.getClaimAsString("email"));
            return Mono.just(userInfoResponse);
        }

        log.error("Can not get current user info: Invalid principal");
        return Mono.error(new ApiException("Can not get current user info: Invalid principal"));
    }

    private KeycloakUserRepresentation getKeycloakUserRepresentation(GatewayRegistrationRequestDto request) {
        return new KeycloakUserRepresentation(
                null,
                request.getEmail(),
                request.getEmail(),
                true,
                true,
                null
        );
    }

    private KeycloakCredentialsRepresentation getKeycloakCredentialsRepresentation(GatewayRegistrationRequestDto request) {
        return new KeycloakCredentialsRepresentation(
                "password",
                request.getPassword(),
                false
        );
    }

    @WithSpan("userService.register")
    public Mono<TokenResponse> register(GatewayRegistrationRequestDto request) {
        return keycloakClient.adminLogin()
                .flatMap(adminToken ->
                        keycloakClient.registerUser(adminToken, getKeycloakUserRepresentation(request))
                                .flatMap(principalId ->
                                        keycloakClient.resetUserPassword(principalId,
                                                        getKeycloakCredentialsRepresentation(request),
                                                        adminToken.getAccessToken())
                                                .thenReturn(principalId)
                                )
                                .flatMap(principalId ->
                                        profileService.register(request, principalId)
                                                .onErrorResume(error -> Mono.when(
                                                                //profileService.compensateRegistration(principalId),
                                                                keycloakClient.deleteUser(adminToken, principalId)
                                                        )
                                                        .then(Mono.error(error)))
                                )
                                .flatMap(registrationResponse -> keycloakClient.login(new UserLoginRequest(request.getEmail(), request.getPassword())))
                                .onErrorResume(error -> Mono.error(new ApiException("Can not login user: " + error.getMessage())))
                                .map(tokenResponseMapper::toTokenResponse)

                )
                .onErrorResume(error -> Mono.error(new ApiException("Can not get Keycloak admin token: " + error.getMessage())));
    }

    //    @WithSpan("userService.register")
//    public Mono<TokenResponse> register(GatewayRegistrationRequestDto request) {
//        return profileService.register(request)
//                .flatMap(registrationResponseDto ->
//                        keycloakClient.adminLogin()
//                                .flatMap(adminToken ->
//                                     keycloakClient.registerUser(adminToken, getKeycloakUserRepresentation(request))
//                                            .flatMap(kcUserId ->
//                                                    keycloakClient.resetUserPassword(kcUserId,
//                                                                    getKeycloakCredentialsRepresentation(request),
//                                                                    adminToken.getAccessToken())
//                                                            .thenReturn(kcUserId)
//                                            )
//                                            .flatMap(v ->
//                                                    keycloakClient.login(new UserLoginRequest(request.getEmail(), request.getPassword())))
//                                            .onErrorResume(error ->
//                                                    profileService.compensateRegistration(registrationResponseDto.getId().toString())
//                                                            .then(Mono.error(error)))
//                                            .map(tokenResponseMapper::toTokenResponse)
//                                )
//                )
//                .onErrorResume(error -> Mono.error(new ApiException("Can not register user: " + error.getMessage())));
//    }
}
