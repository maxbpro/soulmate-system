package ru.maxb.soulmate.gateway.mapper;

import org.mapstruct.Mapper;
import ru.maxb.soulmate.gateway.dto.ProfileDto;
import ru.maxb.soulmate.profile.dto.ProfileRegistrationRequestDto;
import ru.maxb.soulmate.profile.dto.ProfileRegistrationResponseDto;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface ProfileMapper {

    ru.maxb.soulmate.profile.dto.ProfileRegistrationRequestDto from(ru.maxb.soulmate.gateway.dto.ProfileRegistrationRequestDto dto);

    ru.maxb.soulmate.profile.dto.ProfileDto from(ProfileDto dto);

    ProfileDto from(ru.maxb.soulmate.profile.dto.ProfileDto dto);

    ru.maxb.soulmate.profile.dto.ProfileRegistrationResponseDto from(ProfileRegistrationResponseDto dto);
}
