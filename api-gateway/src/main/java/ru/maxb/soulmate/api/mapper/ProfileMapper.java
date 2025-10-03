package ru.maxb.soulmate.api.mapper;

import org.mapstruct.Mapper;
import ru.maxb.soulmate.profile.dto.IndividualDto;
import ru.maxb.soulmate.profile.dto.IndividualWriteDto;
import ru.maxb.soulmate.profile.dto.IndividualWriteResponseDto;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface ProfileMapper {

    net.proselyte.person.dto.IndividualWriteDto from(IndividualWriteDto dto);

    net.proselyte.person.dto.IndividualDto from(IndividualDto dto);

    IndividualDto from(net.proselyte.person.dto.IndividualDto dto);

    IndividualWriteResponseDto from(net.proselyte.person.dto.IndividualWriteResponseDto dto);
}
