package ru.maxb.soulmate.swipe.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.model.SwipeEntity;
import ru.maxb.soulmate.swipe.util.DateTimeUtil;

import java.util.UUID;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class SwipeMapper {

    protected DateTimeUtil dateTimeUtil;

    @Autowired
    public void setDateTimeUtil(DateTimeUtil dateTimeUtil) {
        this.dateTimeUtil = dateTimeUtil;
    }

    @Mapping(target = "createdAt", expression = "java(dateTimeUtil.now())")
    public abstract SwipeEntity from(String userPair, UUID userId, SwipeRequestDto dto);

    @Mapping(target = "id", source = "userPair")
    public abstract SwipeDto from(SwipeEntity swipeEntity);
}
