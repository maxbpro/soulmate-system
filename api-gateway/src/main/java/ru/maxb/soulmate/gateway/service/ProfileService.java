package ru.maxb.soulmate.gateway.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationResponseDto;
import ru.maxb.soulmate.gateway.exception.ProfileServiceException;
import ru.maxb.soulmate.gateway.mapper.ProfileMapper;
import ru.maxb.soulmate.user.api.ProfileApiClient;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileApiClient profileApiClient;
    private final ProfileMapper profileMapper;

    // Timeout configuration (could be externalized)
    private static final int REGISTRATION_TIMEOUT_SECONDS = 30;
    private static final int COMPENSATION_TIMEOUT_SECONDS = 10;

    public Mono<Void> register(GatewayRegistrationRequestDto request, String principalId) {
        log.info("Starting profile registration for user: {}", principalId);

        ProfileRegistrationRequestDto profileRequest = profileMapper.from(request, principalId);

        return Mono.fromCallable(() -> profileApiClient.registration(profileRequest))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(response -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        return Mono.error(
                                new ProfileServiceException("Profile service returned error: " + response.getStatusCode()));
                    }

                    if (response.getBody() == null) {
                        return Mono.error(
                                new ProfileServiceException("Profile service returned empty body"
                        ));
                    }

                    return Mono.just(response.getBody());
                })
                .map(profileMapper::fromProfileDto)
                .doOnSuccess(response -> {
                    log.info("Profile registered successfully: id={}, user={}", response.getId(), principalId);
                })
                .doOnError(error -> {log.error("Profile registration failed for user: {}", principalId, error);
                })
                .timeout(Duration.ofSeconds(REGISTRATION_TIMEOUT_SECONDS))
                .then();
    }

    @WithSpan
    public Mono<Void> compensateRegistration(String id) {
        log.info("Starting compensation for profile: {}", id);
        return Mono.fromRunnable(() ->  profileApiClient.compensateRegistration(UUID.fromString(id)))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(v -> {
                    log.info("Profile compensation successful for: {}", id);
                })
                .doOnError(error -> {
                    log.error("Profile compensation failed for: {}", id, error);
                })
                .onErrorResume(error -> {
                    // Log but don't propagate compensation errors
                    // We don't want compensation failures to break the rollback chain
                    log.warn("Compensation error swallowed for profile {}: {}", id, error.getMessage());
                    return Mono.empty();
                })
                .timeout(java.time.Duration.ofSeconds(COMPENSATION_TIMEOUT_SECONDS))
                .then();
    }
}
