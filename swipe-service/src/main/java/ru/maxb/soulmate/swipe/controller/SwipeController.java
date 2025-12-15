package ru.maxb.soulmate.swipe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.swipe.api.SwipeApi;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.service.SwipeService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SwipeController implements SwipeApi {

    private final SwipeService swipeService;

    @Override
    public ResponseEntity<SwipeDto> swipe(@Valid SwipeRequestDto swipeRequestDto) {
        UUID userId = null; //todo
        return ResponseEntity.ok(swipeService.createSwipe(userId, swipeRequestDto));
    }
}
