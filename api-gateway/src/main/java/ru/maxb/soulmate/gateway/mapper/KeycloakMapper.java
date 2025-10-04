package ru.maxb.soulmate.gateway.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.maxb.soulmate.gateway.dto.TokenRefreshRequest;
import ru.maxb.soulmate.gateway.dto.UserLoginRequest;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public abstract class KeycloakMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    public abstract ru.maxb.soulmate.keycloak.dto.UserLoginRequest
    toKeycloakUserLoginRequest(UserLoginRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "refreshToken", source = "refreshToken")
    public abstract ru.maxb.soulmate.keycloak.dto.TokenRefreshRequest
    toKeycloakTokenRefreshRequest(TokenRefreshRequest request);
}
