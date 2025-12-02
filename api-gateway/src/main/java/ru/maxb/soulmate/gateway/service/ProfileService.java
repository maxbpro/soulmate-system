package ru.maxb.soulmate.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

    public GatewayRegistrationResponseDto register(GatewayRegistrationRequestDto request) {
        ProfileRegistrationRequestDto from = profileMapper.from(request);
        ResponseEntity<ProfileRegistrationResponseDto> response2 = null;
//        try {

        response2 = profileApiClient.registration(from);

//        } catch (Exception e) {
//            e.printStackTrace();
//            // Handle the specific FeignException
//        }

        ResponseEntity<ProfileRegistrationResponseDto> response = profileApiClient.registration(from);
        ProfileRegistrationResponseDto profileRegistrationResponseDto = response.getBody();
        GatewayRegistrationResponseDto gatewayRegistrationResponseDto = profileMapper.from(profileRegistrationResponseDto);

        log.info("Person registered id = [{}]", gatewayRegistrationResponseDto.getId());
        return gatewayRegistrationResponseDto;
    }

    //    @WithSpan("personService.compensateRegistration")
    public void compensateRegistration(String id) {
        profileApiClient.compensateRegistration(UUID.fromString(id));
    }
}
