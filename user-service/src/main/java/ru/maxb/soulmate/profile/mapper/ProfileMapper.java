package ru.maxb.soulmate.profile.mapper;

import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import ru.maxb.soulmate.landmark.dto.LandmarkMatchDto;
import ru.maxb.soulmate.profile.model.ProfileEntity;
import ru.maxb.soulmate.profile.util.DateTimeUtil;
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

    protected DateTimeUtil dateTimeUtil;

    @Mapping(target = "id", source = "request.principalId")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "created", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "landmarks", source = "landmarks")
    @Mapping(target = "photos", ignore = true)
    public abstract ProfileEntity to(ProfileRegistrationRequestDto request, String landmarks);

    public abstract ProfileDto from(ProfileEntity profileEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "landmarks", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    public abstract void update(
            @MappingTarget
            ProfileEntity profileEntity,
            ProfileRegistrationRequestDto dto
    );

    @Mapping(target = "radius", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "interestedIn", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "ageMin", ignore = true)
    @Mapping(target = "ageMax", ignore = true)
    public abstract ProfileDto toProfileDto(LandmarkMatchDto landmarkMatchDto);
}
