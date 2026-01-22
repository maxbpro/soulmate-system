package ru.maxb.soulmate.gateway.service;

import io.opentelemetry.api.trace.Span;
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
import ru.maxb.soulmate.gateway.exception.KeycloakClientException;
import ru.maxb.soulmate.gateway.exception.UserAlreadyExistsException;
import ru.maxb.soulmate.gateway.mapper.TokenResponseMapper;
import ru.maxb.soulmate.keycloak.dto.KeycloakTokenResponse;
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
                .switchIfEmpty(Mono.error(new ApiException("No authentication present")))
                .doOnSuccess(response ->
                        log.debug("User info retrieved for user: {}", response.getEmail()))
                .doOnError(e ->
                        log.error("Failed to get user info", e));
    }

    @WithSpan("userService.register")
    public Mono<TokenResponse> register(GatewayRegistrationRequestDto request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        return validateRegistrationRequest(request)
                .then(Mono.defer(keycloakClient::getOrRefreshAdminToken))
                .flatMap(adminToken -> {
                    Span.current().setAttribute("user.email", request.getEmail());

                    return registerUserInKeycloak(request, adminToken)
                            .flatMap(userId -> {
                                Span.current().setAttribute("user.keycloak_id", userId);
                                return completeRegistration(request, userId, adminToken);
                            })
                            .onErrorResume(error -> handleRegistrationError(error, request));
                })
                //.map(v -> tokenResponseMapper.toTokenResponse(v.))
                .doOnSuccess(token ->
                        log.info("Registration successful for email: {}", request.getEmail()))
                .doOnError(error -> {
                    log.error("Registration failed for email: {}", request.getEmail(), error);
                    Span.current().recordException(error);
                });
    }

    private Mono<String> registerUserInKeycloak(GatewayRegistrationRequestDto request, String adminToken) {
        KeycloakUserRepresentation user = getKeycloakUserRepresentation(request);

        return keycloakClient.registerUser(adminToken, user)
                .onErrorResume(KeycloakClientException.class,
                        e -> Mono.error(new ApiException("Failed to register user in Keycloak: " + e.getMessage(), e)))
                .onErrorResume(UserAlreadyExistsException.class,
                        Mono::error);
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


    private Mono<TokenResponse> handleRegistrationError(Throwable error, GatewayRegistrationRequestDto request) {
        if (error instanceof UserAlreadyExistsException) {
            return Mono.error(error);
        }

        // Log and wrap in appropriate exception
        log.error("Registration failed for email: {}", request.getEmail(), error);

        String errorMessage = "Registration failed";
        if (error.getMessage() != null) {
            errorMessage += ": " + error.getMessage();
        }

        return Mono.error(new ApiException(errorMessage, error));
    }

    private Mono<Void> validateRegistrationRequest(GatewayRegistrationRequestDto request) {
        return Mono.fromRunnable(() -> {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new ApiException("Email is required");
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new ApiException("Password is required");
            }
        });
    }

    private Mono<TokenResponse> completeRegistration(GatewayRegistrationRequestDto request,
                                                     String userId, String adminToken) {
        return resetUserPassword(request, userId, adminToken)
                .then(registerProfile(request, userId))
                .then(loginAfterRegistration(request))
                .map(tokenResponseMapper::toTokenResponse)
                .onErrorResume(error -> rollbackRegistration(userId, adminToken, error));
    }

    private Mono<Void> resetUserPassword(GatewayRegistrationRequestDto request, String userId, String adminToken) {
        KeycloakCredentialsRepresentation credentials = getKeycloakCredentialsRepresentation(request);

        return keycloakClient.resetUserPassword(userId, credentials, adminToken)
                .doOnSuccess(v -> log.debug("Password set for user: {}", userId))
                .onErrorResume(e -> {
                    log.error("Failed to set password for user: {}", userId, e);
                    return Mono.error(new ApiException("Failed to set user password: " + e.getMessage(), e));
                });
    }

    private Mono<Void> registerProfile(GatewayRegistrationRequestDto request, String userId) {
        return profileService.register(request, userId)
                .doOnSuccess(v -> log.debug("Profile created for user: {}", userId))
                .onErrorResume(e -> {
                    log.error("Failed to create profile for user: {}", userId, e);
                    return Mono.error(new ApiException("Failed to create user profile: " + e.getMessage(), e));
                });
    }

    private Mono<KeycloakTokenResponse> loginAfterRegistration(GatewayRegistrationRequestDto request) {
        UserLoginRequest userLoginRequest = new UserLoginRequest(
                request.getEmail(),
                request.getPassword()
        );

        return keycloakClient.login(userLoginRequest)
                .timeout(java.time.Duration.ofSeconds(30))
                .onErrorResume(e -> Mono.error(
                        new ApiException("Failed to login after registration: " + e.getMessage(), e)));
    }

    private Mono<TokenResponse> rollbackRegistration(String userId, String adminToken, Throwable error) {
        log.warn("Registration failed, rolling back for user: {}", userId, error);

        Mono<Void> compensateMono = Mono.when(
                profileService.compensateRegistration(userId)
                        .onErrorResume(e -> {
                            log.error("Failed to rollback profile for user: {}", userId, e);
                            return Mono.empty();
                        }),
                keycloakClient.executeDeleteOnError(userId, adminToken, error)
                        .onErrorResume(e -> {
                            log.error("Failed to delete Keycloak user during rollback: {}", userId, e);
                            return Mono.empty();
                        })
        ).then();

        return compensateMono.then(Mono.error(error));
    }
}
