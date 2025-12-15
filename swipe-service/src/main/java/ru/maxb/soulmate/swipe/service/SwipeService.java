package ru.maxb.soulmate.swipe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.mapper.MatchMapper;
import ru.maxb.soulmate.swipe.mapper.SwipeMapper;
import ru.maxb.soulmate.swipe.model.MatchEntity;
import ru.maxb.soulmate.swipe.model.SwipeEntity;
import ru.maxb.soulmate.swipe.repository.MatchRepository;
import ru.maxb.soulmate.swipe.repository.SwipeRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwipeService {

    private final SwipeMapper swipeMapper;
    private final MatchMapper matchMapper;
    private final SwipeRepository swipeRepository;
    private final MatchRepository matchRepository;

    public SwipeDto createSwipe(UUID profileId, SwipeRequestDto requestDto) {
        UUID swipedUserId = requestDto.getSwipedUserId();
        String userPair = String.format("%s:%s", profileId, swipedUserId);

        SwipeEntity swipeEntity = swipeMapper.from(userPair, profileId, requestDto);
        swipeRepository.save(swipeEntity);
        log.info("Swipe created: {}", swipeEntity.getUserPair());

        boolean hasMatch = swipeRepository.hasMatch(String.format("%s:%s", swipedUserId, profileId),
                swipedUserId, profileId).orElse(false);

        if (hasMatch) {
            createMatch(swipeEntity);
        }

        return swipeMapper.from(swipeEntity);
    }

    private void createMatch(SwipeEntity swipeEntity) {
        MatchEntity mapperEntity = matchMapper.toEntity(UUID.randomUUID(), swipeEntity);
        MatchEntity matchEntity = matchRepository.save(mapperEntity);
        log.info("Match created: {}", matchEntity.getId());
    }
}
