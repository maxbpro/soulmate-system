package ru.maxb.soulmate.swipe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.maxb.soulmate.swipe.dto.MatchDto;
import ru.maxb.soulmate.swipe.model.MatchEntity;
import ru.maxb.soulmate.swipe.model.SwipeEntity;
import ru.maxb.soulmate.swipe.util.DateTimeUtil;

import java.util.UUID;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public abstract class MatchMapper {

    @Autowired
    public DateTimeUtil dateTimeUtil;

    public abstract MatchDto toDto(MatchEntity match);

    @Mapping(target = "soulmateId", source = "swipeEntity.swipedUserId")
    @Mapping(target = "createdAt", expression = "java(dateTimeUtil.now())")
    public abstract MatchEntity toEntity(UUID id, SwipeEntity swipeEntity);
}
