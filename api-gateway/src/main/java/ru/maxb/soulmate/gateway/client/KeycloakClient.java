package ru.maxb.soulmate.gateway.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import reactor.util.retry.Retry;
import ru.maxb.soulmate.gateway.config.KeycloakProperties;
import ru.maxb.soulmate.gateway.dto.KeycloakCredentialsRepresentation;
import ru.maxb.soulmate.gateway.dto.KeycloakUserRepresentation;
import ru.maxb.soulmate.gateway.exception.ApiException;
import ru.maxb.soulmate.gateway.exception.KeycloakClientException;
import ru.maxb.soulmate.gateway.exception.UserAlreadyExistsException;
import ru.maxb.soulmate.gateway.util.UserIdExtractor;
import ru.maxb.soulmate.keycloak.dto.TokenRefreshRequest;
import ru.maxb.soulmate.keycloak.dto.KeycloakTokenResponse;
import ru.maxb.soulmate.keycloak.dto.UserLoginRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakClient {

    private static final String BEARER_PREFIX = "Bearer ";

    private final WebClient webClient;
    private final KeycloakProperties props;

    private String userRegistrationUrl;
    private String userPasswordResetUrl;
    private String userDeleteUrl;
    private String userByIdUrl;
    private String userSearchUrl;

    @Value("${keycloak.client.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${keycloak.client.retry-attempts:3}")
    private int retryAttempts;

    private volatile KeycloakTokenResponse cachedAdminToken;
    private volatile long tokenExpiryTime;

    @PostConstruct
    public void init() {
        String adminRealmUrl = String.format("%s/admin/realms/%s",
                props.serverUrl(), props.realm());

        this.userRegistrationUrl = adminRealmUrl + "/users";
        this.userDeleteUrl = adminRealmUrl + "/users/{id}";
        this.userByIdUrl = userRegistrationUrl + "/{id}";
        this.userPasswordResetUrl = userByIdUrl + "/reset-password";
        this.userSearchUrl = userRegistrationUrl + "?email={email}";

        log.info("Keycloak client initialized. Realm: {}, Server: {}, Timeout: {}s",
                props.realm(), props.serverUrl(), timeoutSeconds);
    }

    @WithSpan
    public Mono<String> getOrRefreshAdminToken() {
        return Mono.defer(() -> {
            if (cachedAdminToken != null && !isTokenExpired(cachedAdminToken)) {
                return Mono.just(cachedAdminToken.getAccessToken());
            }
            return adminLogin()
                    .doOnNext(token -> {
                        cachedAdminToken = token;
                        // Calculate expiry (assuming 300s default, with 30s buffer)
                        tokenExpiryTime = System.currentTimeMillis() + (token.getExpiresIn() - 30) * 1000;
                        log.debug("Admin token refreshed and cached");
                    })
                    .map(KeycloakTokenResponse::getAccessToken);
        });
    }

    private boolean isTokenExpired(KeycloakTokenResponse token) {
        if (token == null) return true;
        return System.currentTimeMillis() >= tokenExpiryTime;
    }

    @WithSpan
    public Mono<KeycloakTokenResponse> login(UserLoginRequest req) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("username", req.getEmail());
        form.add("password", req.getPassword());
        form.add("client_id", props.clientId());
        addIfNotBlank(form, "client_secret", props.clientSecret());

        log.debug("Login attempt for user: {}", req.getEmail());

        return webClient.post()
                .uri(props.tokenUrl()) // http://localhost:8080/realms/individual/protocol/openid-connect/token
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> handleKeycloakError(response, "Login failed"))
                .bodyToMono(KeycloakTokenResponse.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .retryWhen(retryOnTimeout(retryAttempts))
                .doOnSuccess(token -> {
                    log.info("Login successful for user: {}", req.getEmail());
                })
                .doOnError(e -> {
                    log.error("Login failed for user: {}", req.getEmail(), e);
                })
                .onErrorMap(this::mapToApiException);
    }

    @WithSpan
    public Mono<KeycloakTokenResponse> adminLogin() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("client_id", props.adminClientId());
        form.add("username", props.adminUsername());
        form.add("password", props.adminPassword());

        return webClient.post()
                .uri(props.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> handleKeycloakError(response, "Admin login failed"))
                .bodyToMono(KeycloakTokenResponse.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnSuccess(token -> {
                    //todo Cache the token
                    log.debug("Admin token obtained");
                })
                .doOnError(e -> {
                    log.error("Admin login failed", e);
                })
                .onErrorMap(this::mapToApiException);
    }

    @WithSpan
    public Mono<KeycloakTokenResponse> refreshToken(TokenRefreshRequest req) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", req.getRefreshToken());
        form.add("client_id", props.clientId());
        addIfNotBlank(form, "client_secret", props.clientSecret());

        return webClient.post()
                .uri(props.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toApiException)
                .bodyToMono(KeycloakTokenResponse.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnSuccess(token -> {
                    log.debug("Token refreshed successfully");
                })
                .doOnError(e -> {
                    log.warn("Token refresh failed", e);
                })
                .onErrorMap(this::mapToApiException);
    }

    @WithSpan
    public Mono<String> registerUser(String accessToken, KeycloakUserRepresentation user) {
        return webClient.post()
                .uri(userRegistrationUrl)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchangeToMono(response -> handleUserRegistrationResponse(response, user.email()))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnSuccess(userId -> {
                    log.info("User registered successfully with ID: {}", userId);
                })
                .doOnError(e -> {
                    log.error("User registration failed for email: {}", user.email(), e);
                })
                .onErrorMap(this::mapToApiException);
    }

    @WithSpan
    public Mono<Void> deleteUser(KeycloakTokenResponse adminToken, String userId) {
        return webClient.delete()
                .uri(userDeleteUrl, userId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminToken.getAccessToken())
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new ApiException(
                                        "KC user deleting failed " + resp.statusCode())))
                )
                .toBodilessEntity()
                .then()
                .doOnSuccess(v -> {
                    log.info("User deleted successfully: {}", userId);
                })
                .doOnError(e -> {
                    log.error("User delete failed: {}", userId, e);
                });
    }

    @WithSpan
    public Mono<Void> resetUserPassword(String userId, KeycloakCredentialsRepresentation dto, String adminAccessToken) {
        return webClient.put()
                .uri(userPasswordResetUrl, userId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new ApiException(
                                        "KC reset-password failed " + resp.statusCode() + ": " + body)))
                )
                .toBodilessEntity()
                .then()
                .doOnSuccess(v -> log.info("Password reset for user: {}", userId));
    }


    @WithSpan("keycloakClient.resetUserPassword.executeDeleteOnError")
    public Mono<Void> executeDeleteOnError(String userId, String adminAccessToken, Throwable e) {
        return webClient.delete()
                .uri(userByIdUrl, userId)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminAccessToken)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    private static void addIfNotBlank(LinkedMultiValueMap<String, String> form, String key, String value) {
        if (value != null && !value.isBlank()) form.add(key, value);
    }

    private Mono<? extends Throwable> toApiException(ClientResponse resp) {
        return resp.bodyToMono(String.class)
                .defaultIfEmpty(resp.statusCode().toString())
                .map(body -> new ApiException("Keycloak error " + resp.statusCode() + ": " + body));
    }

    private Mono<String> handleUserRegistrationResponse(ClientResponse response, String email) {
        HttpStatusCode statusCode = response.statusCode();

        if (statusCode.equals(HttpStatus.CREATED)) {
            return extractIdFromPath(response);
        }

        // Handle 409 Conflict - User already exists
        if (statusCode.equals(HttpStatus.CONFLICT)) {
            return response.bodyToMono(String.class)
                    .defaultIfEmpty("User already exists")
                    .flatMap(body -> {
                        log.warn("User already exists in Keycloak: {}", email);
                        String errorMessage = extractErrorMessage(body, email);
                        return Mono.error(new UserAlreadyExistsException(errorMessage));
                    });
        }
        // Handle other errors
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Unknown error")
                .flatMap(body -> {
                    String errorMessage = String.format(
                            "User registration failed for email %s - Status: %s, Body: %s",
                            email, statusCode, body);

                    return Mono.error(new KeycloakClientException(errorMessage, statusCode, body));
                });
    }


    private Mono<String> extractIdFromPath(ClientResponse response) {
        if (response.statusCode().equals(HttpStatus.CREATED)) {
            var location = response.headers().asHttpHeaders().getLocation();
            if (location == null)
                throw new ApiException("Location header missing");
            return Mono.just(UserIdExtractor.extractIdFromPath(location.getPath()));
        }
        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new ApiException("User creation failed: " + body)));
    }

    private String extractErrorMessage(String responseBody, String email) {
        try {
            // Keycloak typically returns JSON error responses
            if (responseBody.contains("User exists")) {
                return String.format("User with email '%s' already exists", email);
            }

            // Try to parse as JSON for more detailed message
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            if (root.has("errorMessage")) {
                return root.get("errorMessage").asText();
            }

            if (root.has("error")) {
                String error = root.get("error").asText();
                return String.format("Keycloak error: %s", error);
            }

        } catch (Exception e) {
            log.debug("Failed to parse Keycloak error response", e);
        }

        // Default message
        return String.format("User with email '%s' already exists", email);
    }

    private Mono<? extends Throwable> handleKeycloakError(ClientResponse response, String context) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("No error body")
                .flatMap(body -> {
                    HttpStatusCode statusCode = response.statusCode();
                    String errorMessage = String.format("%s - Status: %s, Body: %s",
                            context, statusCode, body);

                    log.warn(errorMessage);

                    // Map specific Keycloak errors
                    if (statusCode.equals(HttpStatus.UNAUTHORIZED)) {
                        return Mono.error(new KeycloakClientException(
                                "Invalid credentials or token", statusCode, body));
                    } else if (statusCode.equals(HttpStatus.CONFLICT)) {
                        return Mono.error(new KeycloakClientException(
                                "User already exists", statusCode, body));
                    } else if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new KeycloakClientException(
                                "Resource not found", statusCode, body));
                    } else if (statusCode.value() >= 500) {
                        return Mono.error(new KeycloakClientException(
                                "Keycloak server error", statusCode, body));
                    } else {
                        return Mono.error(new KeycloakClientException(
                                errorMessage, statusCode, body));
                    }
                });
    }

    private Retry retryOnTimeout(int maxAttempts) {
        return reactor.util.retry.Retry.backoff(maxAttempts, Duration.ofSeconds(1))
                .filter(throwable -> throwable instanceof TimeoutException)
                .doBeforeRetry(retrySignal ->
                        log.warn("Retrying Keycloak request after timeout. Attempt: {}",
                                retrySignal.totalRetries() + 1))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                        new ApiException("Keycloak request failed after " + maxAttempts + " attempts"));
    }

    private Throwable mapToApiException(Throwable e) {
        if (e instanceof TimeoutException) {
            return new ApiException("Keycloak request timeout", e);
        } else if (e instanceof KeycloakClientException) {
            return new ApiException(e.getMessage(), e);
        } else {
            return new ApiException("Keycloak client error", e);
        }
    }
}
