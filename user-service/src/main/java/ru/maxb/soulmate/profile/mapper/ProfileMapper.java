package ru.maxb.soulmate.profile.mapper;

import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import ru.maxb.soulmate.profile.model.ProfileEntity;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = CONSTRUCTOR
)
@Setter(onMethod_ = @Autowired)
public abstract class ProfileMapper {

    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    public abstract ProfileEntity to(ProfileRegistrationRequestDto registrationRequestDto);

    public abstract ProfileDto from(ProfileEntity profileEntity);

    public abstract void update(
            @MappingTarget
            ProfileEntity profileEntity,
            ProfileRegistrationRequestDto dto
    );
}
