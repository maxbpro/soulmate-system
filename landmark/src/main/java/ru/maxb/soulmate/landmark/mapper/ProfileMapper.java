package ru.maxb.soulmate.landmark.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.maxb.soulmate.common.event.ProfileCreatedDto;
import ru.maxb.soulmate.landmark.model.Profile;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface ProfileMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", source = "profileEvent.id")
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "dateOfBirth", source = "profileEvent.birthDate")
    //@Mapping(target = "location.lat", source = "profileEvent.lat")
    //@Mapping(target = "location.lon", source = "profileEvent.lng")
    Profile toProfile(ProfileCreatedDto profileEvent);
}
