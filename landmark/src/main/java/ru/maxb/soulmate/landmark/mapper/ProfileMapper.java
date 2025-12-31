package ru.maxb.soulmate.landmark.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import ru.maxb.soulmate.common.event.ProfileCreatedDto;
import ru.maxb.soulmate.landmark.model.Profile;
import ru.maxb.soulmate.landmark.util.DateTimeUtil;

import static org.mapstruct.InjectionStrategy.SETTER;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING,
        injectionStrategy = SETTER,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ProfileMapper {

    protected DateTimeUtil dateTimeUtil;

    @Autowired
    public void setDateTimeUtil(DateTimeUtil dateTimeUtil) {
        this.dateTimeUtil = dateTimeUtil;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", source = "profileEvent.id")
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "dateOfBirth", source = "profileEvent.birthDate")
    @Mapping(target = "created", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    public abstract Profile toProfile(ProfileCreatedDto profileEvent);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "radius", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "landmarks", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "ageMin", ignore = true)
    @Mapping(target = "ageMax", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "location", source = "location")
    public abstract void updateLocation(
            @MappingTarget
            Profile profile,
            GeoPoint location
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "landmarks", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "radius", source = "profileCreatedDto.radius")
    @Mapping(target = "dateOfBirth", source = "profileCreatedDto.birthDate")
    @Mapping(target = "ageMin", source = "profileCreatedDto.ageMin")
    @Mapping(target = "ageMax", source = "profileCreatedDto.ageMax")
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    public abstract void update(
            @MappingTarget
            Profile profile,
            ProfileCreatedDto profileCreatedDto
    );
}
