package ru.maxb.soulmate.swipe.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.exception.SwipeException;
import ru.maxb.soulmate.swipe.mapper.MatchMapper;
import ru.maxb.soulmate.swipe.mapper.SwipeMapper;
import ru.maxb.soulmate.swipe.model.MatchEntity;
import ru.maxb.soulmate.swipe.model.SwipeEntity;
import ru.maxb.soulmate.swipe.repository.MatchRepository;
import ru.maxb.soulmate.swipe.repository.SwipeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwipeService {

    private final SwipeMapper swipeMapper;
    private final MatchMapper matchMapper;
    private final SwipeRepository swipeRepository;
    private final MatchRepository matchRepository;

    /**
     * SWIPE MATCHING LOGIC:
     * <p>
     * We store swipes in "swiper:swiped" format (not sorted).
     * Example: User A swiping User B creates partition "A:B"
     * <p>
     * When checking for matches, we look in the REVERSE partition:
     * - User B swiping User A checks partition "A:B" for A→B swipe
     * <p>
     * This creates duplicate partitions (A:B and B:A) but simplifies
     * matching logic at the cost of storage efficiency.
     * <p>
     * Pros:
     * - Simple implementation
     * - Fast match checking
     * - No sorting overhead
     * <p>
     * Cons:
     * - Duplicate data storage
     * - Complex queries across all user swipes
     */
    @WithSpan
    public SwipeDto createSwipe(UUID swiperId, SwipeRequestDto requestDto) {
        log.info("Starting swipe creation - swiperId: {}, targetId: {}, liked: {}",
                swiperId, requestDto.getSwipedUserId(), requestDto.getLiked());

        // Check for self-swipe
        if (swiperId.equals(requestDto.getSwipedUserId())) {
            log.warn("Self-swipe attempt detected - userId: {}", swiperId);
            throw new SwipeException("Cannot swipe on yourself");
        }

        checkDuplicateSwipe(swiperId, requestDto.getSwipedUserId());

        try {

            UUID swipedUserId = requestDto.getSwipedUserId();
            String userPair = formatUserPair(swiperId, swipedUserId);

            SwipeEntity swipeEntity = swipeMapper.from(userPair, swiperId, requestDto);
            swipeRepository.save(swipeEntity);
            log.info("Swipe created: {}", swipeEntity.getUserPair());

            if (requestDto.getLiked()) {
                boolean hasMatch = swipeRepository.hasReverseSwipe(String.format("%s:%s", swipedUserId, swiperId),
                        swipedUserId, swiperId).orElse(false);

                if (hasMatch) {
                    createMatch(swipeEntity);
                }
            }else{
                log.debug("Dislike happened - pair: {}", userPair);
            }

            log.info("Swipe completed successfully - pair: {}", userPair);
            return swipeMapper.from(swipeEntity);

        } catch (Exception e) {
            log.error("Swipe creation failed - swiperId: {}, targetId: {}, error: {}",
                    swiperId, requestDto.getSwipedUserId(), e.getMessage(), e);
            throw new SwipeException("Swipe creation failed");
        }
    }

    public List<SwipeDto> getSwipes(UUID profileId) {
        return swipeRepository.findByUserId(profileId).stream()
                .map(swipeMapper::from)
                .toList();
    }

    private void createMatch(SwipeEntity swipe) {
        UUID swiperId = swipe.getUserId();
        UUID targetId = swipe.getSwipedUserId();
        String userPair = formatUserPair(swiperId, targetId);

        try {
            log.debug("Creating match from swipe - swiper: {}, target: {}, userPair: {}", swiperId, targetId, userPair);

            MatchEntity mapperEntity = matchMapper.toEntity(swipe, userPair);

            // Check if match already exists to prevent duplicates
            boolean matchExists = matchRepository.existsByUserPair(userPair);

            if (matchExists) {
                log.warn("Match already exists, skipping creation - pair: {}", userPair);
                return;
            }

            matchRepository.save(mapperEntity);

            log.info("Match created successfully - pair: {}, users: {}↔{}", userPair, swiperId, targetId);

        } catch (Exception e) {
            log.error("Failed to create match - swiper: {}, target: {}, error: {}",
                    swiperId, targetId, e.getMessage(), e);
            throw new SwipeException("Creating match failed");
        }
    }

    private void checkDuplicateSwipe(UUID swiperId, UUID targetId) {
        String userPair = String.format("%s:%s", swiperId, targetId);

        // Check if swiper already swiped on this target
        Optional<SwipeEntity> existingSwipe = swipeRepository.findByUserPairAndUserIdAndSwipedUserId(
                userPair, swiperId, targetId);

        if (existingSwipe.isPresent()) {
            log.warn("Duplicate swipe attempt - swiperId: {}, targetId: {}, existingLike: {}",
                    swiperId, targetId, existingSwipe.get().getLiked());
            throw new SwipeException("Duplicate swipe attempt");
        }
    }

    private String formatUserPair(UUID user1, UUID user2) {
        return String.format("%s:%s", user1, user2);
    }
}
