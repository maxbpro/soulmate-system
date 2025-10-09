package ru.maxb.soulmate.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationResponseDto;
import ru.maxb.soulmate.gateway.mapper.ProfileMapper;
import ru.maxb.soulmate.keycloak.api.KeycloakAuthApiClient;
import ru.maxb.soulmate.user.api.ProfileApiClient;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationResponseDto;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileApiClient profileApiClient;
    private final KeycloakAuthApiClient keycloakAuthApiClient;
    private final ProfileMapper profileMapper;

//    @WithSpan("personService.register")
    public Mono<GatewayRegistrationResponseDto> register(GatewayRegistrationRequestDto request) {
        ProfileRegistrationRequestDto from = profileMapper.from(request);
        ResponseEntity<ProfileRegistrationResponseDto> response = profileApiClient.registration(from);
        return Mono.fromCallable(() -> response)
                .mapNotNull(HttpEntity::getBody)
                .map(profileMapper::from)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(t -> log.info("Person registered id = [{}]", t.getId()));
    }

//    @WithSpan("personService.compensateRegistration")
    public Mono<Void> compensateRegistration(String id) {
        return Mono.fromRunnable(() -> profileApiClient.compensateRegistration(UUID.fromString(id)))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
