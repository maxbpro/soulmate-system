package ru.maxb.soulmate.profile.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import ru.maxb.soulmate.profile.model.Gender;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface GenderMapper {

    @ValueMapping(target = "male", source = "MALE")
    @ValueMapping(target = "female", source = "FEMALE")
    Gender toGender(ProfileRegistrationRequestDto.InterestedInEnum interestedIn);
}
