package ru.maxb.soulmate.gateway.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.maxb.soulmate.gateway.mapper.ProfileMapper;
import ru.maxb.soulmate.profile.api.V1Api;
import ru.maxb.soulmate.gateway.dto.ProfileRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.ProfileRegistrationResponseDto;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final V1Api profileApiClient;
    private final ProfileMapper profileMapper;

    @WithSpan("personService.register")
    public Mono<ProfileRegistrationResponseDto> register(ProfileRegistrationRequestDto request) {
        return Mono.fromCallable(() -> profileApiClient.registration(profileMapper.from(request)))
                .mapNotNull(HttpEntity::getBody)
                .map(profileMapper::from)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(t -> log.info("Person registered id = [{}]", t.getId()));
    }

    @WithSpan("personService.compensateRegistration")
    public Mono<Void> compensateRegistration(String id) {
        return Mono.fromRunnable(() -> personApiClient.compensateRegistration(UUID.fromString(id)))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
