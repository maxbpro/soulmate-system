package ru.maxb.soulmate.swipe.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.swipe.common.AbstractCassandraTest;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.model.MatchEntity;
import ru.maxb.soulmate.swipe.service.MatchService;
import ru.maxb.soulmate.swipe.service.SwipeService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class SwipeIntegrationTest extends AbstractCassandraTest {

    @Autowired
    private SwipeService swipeService;

    @Autowired
    private MatchService matchService;

    @Test
    public void createSwipeAndMatch() {
        UUID profileId = UUID.randomUUID();
        UUID swipedUserId = UUID.randomUUID();

        SwipeRequestDto swipeRequestDto = getSwipeRequestDto(swipedUserId);
        SwipeDto swipeDto = swipeService.createSwipe(profileId, swipeRequestDto);

        assertEquals(swipedUserId, swipeDto.getSwipedUserId());
        assertEquals(profileId, swipeDto.getUserId());

        List<MatchEntity> matches = matchService.getMatches(profileId);
        assertEquals(0, matches.size());

        SwipeRequestDto swipeRequest2 = getSwipeRequestDto(profileId);
        SwipeDto swipeDto2 = swipeService.createSwipe(swipedUserId, swipeRequest2);
        assertEquals(profileId, swipeDto2.getSwipedUserId());
        assertEquals(swipedUserId, swipeDto2.getUserId());

        matches = matchService.getMatches(profileId);
        assertEquals(1, matches.size());
        assertEquals(swipedUserId, matches.get(0).getUserId());
        assertEquals(profileId, matches.get(0).getSoulmateId());

        UUID swipedUserId2 = UUID.randomUUID();
        SwipeRequestDto swipeRequest3 = getSwipeRequestDto(swipedUserId2);
        SwipeDto swipeDto3 = swipeService.createSwipe(profileId, swipeRequest3);
        assertEquals(profileId, swipeDto3.getUserId());
        assertEquals(swipedUserId2, swipeDto3.getSwipedUserId());

        matches = matchService.getMatches(profileId);
        assertEquals(1, matches.size());
    }

    private SwipeRequestDto getSwipeRequestDto(UUID swipedUserId) {
        var requestDto = new SwipeRequestDto();
        requestDto.setLiked(true);
        requestDto.setSwipedUserId(swipedUserId);
        return requestDto;
    }
}
