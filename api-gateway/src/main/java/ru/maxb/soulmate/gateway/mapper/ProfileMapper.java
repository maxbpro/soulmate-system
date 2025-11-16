package ru.maxb.soulmate.gateway.mapper;

import org.mapstruct.Mapper;
import ru.maxb.soulmate.gateway.dto.GatewayProfileDto;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationResponseDto;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface ProfileMapper {

    ProfileRegistrationRequestDto from(GatewayRegistrationRequestDto dto);
    GatewayRegistrationResponseDto fromProfileDto(ProfileDto dto);

    GatewayProfileDto from(ProfileDto dto);
    ProfileDto from(GatewayProfileDto dto);
}
