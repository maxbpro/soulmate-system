package ru.maxb.soulmate.swipe.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.swipe.common.AbstractCassandraTest;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.exception.SwipeException;
import ru.maxb.soulmate.swipe.model.MatchEntity;
import ru.maxb.soulmate.swipe.model.SwipeEntity;
import ru.maxb.soulmate.swipe.repository.MatchRepository;
import ru.maxb.soulmate.swipe.repository.SwipeRepository;
import ru.maxb.soulmate.swipe.service.MatchService;
import ru.maxb.soulmate.swipe.service.SwipeService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
public class SwipeIntegrationTest extends AbstractCassandraTest {

    @Autowired
    private SwipeService swipeService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private MatchRepository matchRepository;

    private static UUID userA;
    private static UUID userB;
    private static UUID userC;

    @BeforeEach
    void setUp() {
        userA = UUID.randomUUID();
        userB = UUID.randomUUID();
        userC = UUID.randomUUID();

        // swipeRepository.deleteAll();
        // matchRepository.deleteAll();
    }

    @Test
    void testContainerIsRunningWithNoExceptions() {
        assertThat(cassandra.isRunning()).isTrue();
    }

    @Test
    @Order(1)
    @DisplayName("Should create swipe successfully")
    void shouldCreateSwipe() {
        // Given
        SwipeRequestDto request = createSwipeRequest(userB, true);

        // When
        SwipeDto result = swipeService.createSwipe(userA, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userA);
        assertThat(result.getSwipedUserId()).isEqualTo(userB);
        assertThat(result.getLiked()).isTrue();

        // Verify in database
        List<SwipeEntity> swipes = swipeRepository.findByUserId(userA);
        assertThat(swipes).hasSize(1);
        assertThat(swipes.get(0).getSwipedUserId()).isEqualTo(userB);
    }

    @Test
    @Order(2)
    @DisplayName("Should prevent self-swipe")
    void shouldPreventSelfSwipe() {
        // Given
        SwipeRequestDto request = createSwipeRequest(userA, true);

        // When & Then
        assertThatThrownBy(() -> swipeService.createSwipe(userA, request))
                .isInstanceOf(SwipeException.class)
                .hasMessageContaining("Cannot swipe on yourself");
    }

    @Test
    @Order(3)
    @DisplayName("Should ignore duplicate swipe (same user same target)")
    void shouldIgnoreDuplicateSwipe() {
        // Given - First swipe
        swipeService.createSwipe(userA, createSwipeRequest(userB, true));

        // When - Same swipe again
        assertThatThrownBy(() ->  swipeService.createSwipe(userA, createSwipeRequest(userB, false)))
                .isInstanceOf(SwipeException.class)
                .hasMessageContaining("Duplicate swipe attempt");

        // Then - Should still have only 1 swipe
        List<SwipeEntity> swipes = swipeRepository.findByUserId(userA);
        assertThat(swipes).hasSize(1);
    }

    @Test
    @Order(4)
    @DisplayName("Should create match on mutual like")
    void shouldCreateMatchOnMutualLike() {
        // Given - User A likes User B
        swipeService.createSwipe(userA, createSwipeRequest(userB, true));
        assertThat(matchService.getMatches(userA)).isEmpty();

        // When - User B likes User A (mutual like)
        swipeService.createSwipe(userB, createSwipeRequest(userA, true));

        // Then - Match should be created
        List<MatchEntity> matchesForA = matchService.getMatches(userA);
        List<MatchEntity> matchesForB = matchService.getMatches(userB);

        assertThat(matchesForA).hasSize(1);
        assertThat(matchesForB).hasSize(1);

        MatchEntity match = matchesForA.get(0);
        assertThat(match.getUser1Id()).isEqualTo(userB);
        assertThat(match.getUser2Id()).isEqualTo(userA);
    }


    @Test
    @Order(5)
    @DisplayName("Should NOT create match when only one user likes")
    void shouldNotCreateMatchWhenOnlyOneLikes() {
        // Given - User A likes User B
        swipeService.createSwipe(userA, createSwipeRequest(userB, true));

        // When - User C likes User A (different pair)
        swipeService.createSwipe(userC, createSwipeRequest(userA, true));

        // Then - No matches should exist for A-B pair
        assertThat(matchService.getMatches(userA)).isEmpty();
        assertThat(matchService.getMatches(userB)).isEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("Should NOT create match on pass (dislike)")
    void shouldNotCreateMatchOnPass() {
        // Given - User A likes User B
        swipeService.createSwipe(userA, createSwipeRequest(userB, true));

        // When - User B passes on User A (dislikes)
        swipeService.createSwipe(userB, createSwipeRequest(userA, false));

        // Then - No match should be created
        assertThat(matchService.getMatches(userA)).isEmpty();
    }

    @Test
    @Order(7)
    @DisplayName("Should prevent duplicate match creation")
    void shouldPreventDuplicateMatch() {
        // Given - Mutual like creates match
        swipeService.createSwipe(userA, createSwipeRequest(userB, true));
        swipeService.createSwipe(userB, createSwipeRequest(userA, true));

        List<MatchEntity> initialMatches = matchService.getMatches(userA);
        assertThat(initialMatches).hasSize(1);

        // When - Try to create match again (e.g., due to race condition)
        // This might happen if both users swipe at the same time
        assertThatThrownBy(() ->  swipeService.createSwipe(userA, createSwipeRequest(userB, true)))
                .isInstanceOf(SwipeException.class)
                .hasMessageContaining("Duplicate swipe attempt");

        // Then - Should still have only 1 match
        List<MatchEntity> finalMatches = matchService.getMatches(userA);
        assertThat(finalMatches).hasSize(1);
    }

    @Test
    @Order(8)
    @DisplayName("Should get all swipes for a user")
    void shouldGetAllSwipesForUser() {
        // Given - User A swipes multiple users
        swipeService.createSwipe(userA, createSwipeRequest(userB, true));
        swipeService.createSwipe(userA, createSwipeRequest(userC, false));

        // When
        List<SwipeDto> swipes = swipeService.getSwipes(userA);

        // Then
        assertThat(swipes).hasSize(2);
        assertThat(swipes)
                .extracting(SwipeDto::getSwipedUserId)
                .containsExactlyInAnyOrder(userB, userC);
    }

    @Test
    @Order(9)
    @DisplayName("Should handle multiple users matching independently")
    void shouldHandleMultipleMatches() {
        // Given - Complex scenario
        // A ↔ B (mutual like)
        swipeService.createSwipe(userA, createSwipeRequest(userB, true));
        swipeService.createSwipe(userB, createSwipeRequest(userA, true));

        // A → C (one-sided)
        swipeService.createSwipe(userA, createSwipeRequest(userC, true));

        // B → C (mutual with C)
        swipeService.createSwipe(userB, createSwipeRequest(userC, true));
        swipeService.createSwipe(userC, createSwipeRequest(userB, true));

        // When & Then
        assertThat(matchService.getMatches(userA)).hasSize(1); // Matches with B
        assertThat(matchService.getMatches(userB)).hasSize(2); // Matches with A and C
        assertThat(matchService.getMatches(userC)).hasSize(1); // Matches with B
    }

    @Test
    @Order(10)
    @DisplayName("Should verify match data consistency")
    void shouldVerifyMatchDataConsistency() {
        // Given - Mutual like
        swipeService.createSwipe(userA, createSwipeRequest(userB, true));
        swipeService.createSwipe(userB, createSwipeRequest(userA, true));

        // When
        List<MatchEntity> matches = matchService.getMatches(userA);

        // Then - Verify match entity structure
        assertThat(matches).hasSize(1);

        MatchEntity match = matches.get(0);
        assertThat(match.getUserPair()).isEqualTo(userB + ":" + userA);
        assertThat(match.getUser1Id()).isEqualTo(userB);
        assertThat(match.getUser2Id()).isEqualTo(userA);
        assertThat(match.getCreatedAt()).isNotNull();

        // Verify can find match by userPair
        var matchByPair = matchRepository.findByUserPair(userB + ":" + userA);
        assertThat(matchByPair).isPresent();
    }

    @Test
    @Order(11)
    @DisplayName("Should handle edge case with null liked status")
    void shouldHandleNullLikedStatus() {
        // Given
        SwipeRequestDto request = new SwipeRequestDto();
        request.setSwipedUserId(userB);
        // liked is null

        // When & Then
        assertThatThrownBy(() -> swipeService.createSwipe(userA, request))
                .isInstanceOf(SwipeException.class)
                .hasMessageContaining("Swipe creation failed");
    }

    private SwipeRequestDto createSwipeRequest(UUID targetId, boolean liked) {
        SwipeRequestDto request = new SwipeRequestDto();
        request.setSwipedUserId(targetId);
        request.setLiked(liked);
        return request;
    }
}
