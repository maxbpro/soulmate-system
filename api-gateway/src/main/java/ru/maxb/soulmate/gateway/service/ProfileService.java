package ru.maxb.soulmate.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationResponseDto;
import ru.maxb.soulmate.gateway.exception.ApiException;
import ru.maxb.soulmate.gateway.mapper.ProfileMapper;
import ru.maxb.soulmate.user.api.ProfileApiClient;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileApiClient profileApiClient;
    private final ProfileMapper profileMapper;

    public Mono<GatewayRegistrationResponseDto> register(GatewayRegistrationRequestDto request, String principalId) {
        ProfileRegistrationRequestDto from = profileMapper.from(request, principalId);
        return Mono.fromCallable(() -> profileApiClient.registration(from))
                .mapNotNull(HttpEntity::getBody)
                .map(profileMapper::fromProfileDto)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(t -> log.info("Person registered id = [{}]", t.getId()))
                .onErrorResume(error -> Mono.error(new ApiException("Profile creation failed: " + error.getMessage())));
    }

    //    @WithSpan("personService.compensateRegistration")
    public Mono<Void> compensateRegistration(String id) {
        return Mono.fromRunnable(() ->  profileApiClient.compensateRegistration(UUID.fromString(id)))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
