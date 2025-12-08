package ru.maxb.soulmate.swipe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.mapper.SwipeMapper;
import ru.maxb.soulmate.swipe.model.SwipeEntity;
import ru.maxb.soulmate.swipe.repository.SwipeRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwipeService {

    private final SwipeMapper swipeMapper;
    private final SwipeRepository swipeRepository;

    public SwipeDto createSwipe(SwipeRequestDto requestDto) {
        SwipeEntity swipeEntity = swipeMapper.from(requestDto);
        swipeRepository.save(swipeEntity);

        return swipeMapper.from(swipeEntity);
    }
}
