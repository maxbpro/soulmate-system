package ru.maxb.soulmate.swipe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.swipe.api.MatchApi;
import ru.maxb.soulmate.swipe.dto.MatchDto;
import ru.maxb.soulmate.swipe.mapper.MatchMapper;
import ru.maxb.soulmate.swipe.service.MatchService;
import ru.maxb.soulmate.swipe.util.SecurityUtils;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MatchController implements MatchApi {

    private final MatchService matchService;
    private final MatchMapper matchMapper;
    private final SecurityUtils securityUtils;

    @Override
    public ResponseEntity<List<MatchDto>> match() {
        UUID profileId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(matchService.getMatches(profileId).stream()
                .map(matchMapper::toDto)
                .toList());
    }
}
