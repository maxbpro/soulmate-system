package ru.maxb.soulmate.gateway.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.maxb.soulmate.gateway.dto.GatewayProfileDto;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationResponseDto;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;

import java.util.UUID;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface ProfileMapper {

    @Mapping(target = "principalId", source = "principalId")
    ProfileRegistrationRequestDto from(GatewayRegistrationRequestDto dto, String principalId);

    GatewayRegistrationResponseDto fromProfileDto(ProfileDto dto);

    GatewayProfileDto from(ProfileDto dto);

    @Mapping(target = "radius", ignore = true)
    @Mapping(target = "interestedIn", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "ageMin", ignore = true)
    @Mapping(target = "ageMax", ignore = true)
    ProfileDto from(GatewayProfileDto dto);
}
