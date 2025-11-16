package ru.maxb.soulmate.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationResponseDto;
import ru.maxb.soulmate.gateway.mapper.ProfileMapper;
import ru.maxb.soulmate.keycloak.api.KeycloakAuthApiClient;
import ru.maxb.soulmate.user.api.ProfileApiClient;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileApiClient profileApiClient;
    private final KeycloakAuthApiClient keycloakAuthApiClient;
    private final ProfileMapper profileMapper;

    public GatewayRegistrationResponseDto register(MultipartFile multipartFile,
                                                   GatewayRegistrationRequestDto request) {
        ProfileRegistrationRequestDto from = profileMapper.from(request);
        ResponseEntity<ProfileDto> response2 = null;
//        try {

        response2 = profileApiClient.registration(null, from);

//        } catch (Exception e) {
//            e.printStackTrace();
//            // Handle the specific FeignException
//        }

        ResponseEntity<ProfileDto> response = profileApiClient.registration(multipartFile, from);
        ProfileDto profileDto = response.getBody();

        GatewayRegistrationResponseDto responseDto = profileMapper.fromProfileDto(profileDto);
        log.info("Person registered id = [{}]", responseDto.getId());
        return responseDto;
    }

    //    @WithSpan("personService.compensateRegistration")
    public void compensateRegistration(String id) {
        profileApiClient.compensateRegistration(UUID.fromString(id));
    }
}
