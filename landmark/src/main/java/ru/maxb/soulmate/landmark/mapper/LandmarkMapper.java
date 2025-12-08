package ru.maxb.soulmate.landmark.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.maxb.soulmate.landmark.model.Profile;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface LandmarkMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", source = "profile.profileId")
    @Mapping(target = "landmarksOfProfileId", source = "profile.landmarks")
    @Mapping(target = "soulmateId", source = "otherProfile.profileId")
    @Mapping(target = "landmarksOfSoulmateId", source = "otherProfile.landmarks")
    @Mapping(target = "location", source = "profile.location")
    @Mapping(target = "gender", source = "profile.gender")
    @Mapping(target = "ageMin", source = "profile.ageMin")
    @Mapping(target = "ageMax", source = "profile.ageMin")
    @Mapping(target = "radius", source = "profile.radius")
    @Mapping(target = "dateOfBirth", source = "profile.dateOfBirth")
    LandmarkMatch toLandmarkMatch(Profile profile, Profile otherProfile);
}
