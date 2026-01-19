package ru.maxb.soulmate.swipe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.swipe.api.SwipeApi;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.service.SwipeService;
import ru.maxb.soulmate.swipe.util.SecurityUtils;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SwipeController implements SwipeApi {

    private final SwipeService swipeService;
    private final SecurityUtils securityUtils;

    @Override
    public ResponseEntity<SwipeDto> createSwipe(@Valid SwipeRequestDto swipeRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(swipeService.createSwipe(securityUtils.getCurrentUserId(), swipeRequestDto));

    }

    @Override
    public ResponseEntity<List<SwipeDto>> getSwipes() {
        return ResponseEntity.ok(swipeService.getSwipes(securityUtils.getCurrentUserId()));
    }

}
