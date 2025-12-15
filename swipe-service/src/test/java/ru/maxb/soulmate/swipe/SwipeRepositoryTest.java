package ru.maxb.soulmate.swipe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.mapping.BasicMapId;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.swipe.common.AbstractCassandraTest;
import ru.maxb.soulmate.swipe.model.SwipeEntity;
import ru.maxb.soulmate.swipe.repository.SwipeRepository;
import ru.maxb.soulmate.swipe.util.DateTimeUtil;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class SwipeRepositoryTest extends AbstractCassandraTest {

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @BeforeEach
    public void init() {
        swipeRepository.deleteAll();
    }

    @Test
    public void createSwipe() {
        UUID userId = UUID.randomUUID();
        UUID swipedUserId = UUID.randomUUID();

        String userPair = String.format("%s:%s", userId, swipedUserId);

        SwipeEntity swipe = new SwipeEntity();
        swipe.setUserPair(userPair);
        swipe.setUserId(userId);
        swipe.setSwipedUserId(swipedUserId);
        swipe.setLiked(true);
        swipe.setCreatedAt(dateTimeUtil.now());
        swipeRepository.save(swipe);

        MapId mapId = BasicMapId.id("userPair", userPair)
                .with("userId", userId)
                .with("swipedUserId", swipedUserId);

        Optional<SwipeEntity> byId = swipeRepository.findById(mapId);

        assertEquals(byId.get().getUserPair(), userPair);
        assertEquals(byId.get().getUserId(), userId);
    }

//    @Test
//    public void shouldFindProfile() {
//        Iterable<ProfileEntity> profiles = profileRepository.findAll();
//
//        if (profiles.iterator().hasNext()) {
//
//        }
//    }
}
