package ru.maxb.soulmate.swipe.mapper;

import org.mapstruct.Mapper;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.model.SwipeEntity;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface SwipeMapper {

    SwipeEntity from(SwipeRequestDto dto);

    SwipeDto from(SwipeEntity swipeEntity);
}
