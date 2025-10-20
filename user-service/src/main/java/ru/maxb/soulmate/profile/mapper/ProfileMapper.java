package ru.maxb.soulmate.profile.mapper;

import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import ru.maxb.soulmate.profile.model.ProfileEntity;
import ru.maxb.soulmate.profile.util.DateTimeUtil;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = CONSTRUCTOR,
        uses = {
                GenderMapper.class
        }
)
@Setter(onMethod_ = @Autowired)
public abstract class ProfileMapper {

    protected DateTimeUtil dateTimeUtil;

    @Mapping(target = "active", constant = "true")
    @Mapping(target = "created", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    public abstract ProfileEntity to(ProfileRegistrationRequestDto registrationRequestDto);

    public abstract ProfileDto from(ProfileEntity profileEntity);

    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    public abstract void update(
            @MappingTarget
            ProfileEntity profileEntity,
            ProfileRegistrationRequestDto dto
    );
}
