package ru.maxb.soulmate.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.maxb.soulmate.keycloak.dto.TokenResponse;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface TokenResponseMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "expiresIn", source = "expiresIn")
    @Mapping(target = "tokenType", source = "tokenType")
    ru.maxb.soulmate.profile.dto.TokenResponse toTokenResponse(
            TokenResponse src
    );
}
