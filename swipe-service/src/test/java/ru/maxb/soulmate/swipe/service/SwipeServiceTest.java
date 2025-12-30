package ru.maxb.soulmate.swipe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.mapper.MatchMapperImpl;
import ru.maxb.soulmate.swipe.mapper.SwipeMapperImpl;
import ru.maxb.soulmate.swipe.model.SwipeEntity;
import ru.maxb.soulmate.swipe.repository.MatchRepository;
import ru.maxb.soulmate.swipe.repository.SwipeRepository;
import ru.maxb.soulmate.swipe.util.DateTimeUtil;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwipeServiceTest {

    private SwipeService swipeService;

    @Mock
    private SwipeRepository swipeRepository;

    @Mock
    private MatchRepository matchRepository;

    private final UUID profileId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Clock clock = Clock.system(ZoneOffset.UTC);
        DateTimeUtil dateTimeUtil = new DateTimeUtil(clock);
        SwipeMapperImpl swipeMapper = new SwipeMapperImpl();
        swipeMapper.setDateTimeUtil(dateTimeUtil);
        MatchMapperImpl matchMapper = new MatchMapperImpl();
        matchMapper.setDateTimeUtil(dateTimeUtil);
        swipeService = new SwipeService(
                swipeMapper,
                matchMapper,
                swipeRepository,
                matchRepository
        );
    }

    @Test
    void createSwipe() {
        UUID swipedUserId = UUID.randomUUID();
        SwipeDto swipe = swipeService.createSwipe(profileId, getSwipeRequestDto(swipedUserId));
        assertThat(swipe.getSwipedUserId()).isEqualTo(swipedUserId);
        assertThat(swipe.getUserId()).isEqualTo(profileId);
        assertThat(swipe.getLiked()).isEqualTo(true);
    }

    @Test
    void getSwipes() {
        when(swipeRepository.findByUserId(eq(profileId))).thenReturn(List.of(getSwipeEntity()));
        List<SwipeDto> swipes = swipeService.getSwipes(profileId);
        assertThat(swipes.size()).isEqualTo(1);
    }

    private SwipeRequestDto getSwipeRequestDto(UUID swipedUserId) {
        return new SwipeRequestDto(swipedUserId, true);
    }

    private SwipeEntity getSwipeEntity() {
        SwipeEntity swipeEntity = new SwipeEntity();
        swipeEntity.setUserId(profileId);
        swipeEntity.setLiked(true);
        swipeEntity.setSwipedUserId(UUID.randomUUID());
        return swipeEntity;
    }
}