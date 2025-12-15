package ru.maxb.soulmate.swipe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.swipe.api.MatchApi;
import ru.maxb.soulmate.swipe.dto.MatchDto;

@RestController
@RequiredArgsConstructor
public class MatchController implements MatchApi {

    @Override
    public ResponseEntity<MatchDto> match() {
        return null;
    }
}
