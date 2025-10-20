package ru.maxb.soulmate.swipe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
        SwipeEntity swipe = new SwipeEntity();
        swipe.setId("id " + UUID.randomUUID());
        swipe.setLiked(true);
        swipe.setUserId(userId);
        swipe.setSwipedUserId(UUID.randomUUID());
        swipeRepository.save(swipe);

        Optional<SwipeEntity> byId = swipeRepository.findById(swipe.getId());

        assertEquals(swipe.getId(), byId.get().getId());
        assertEquals(swipe.getUserId(), byId.get().getUserId());
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
