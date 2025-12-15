package ru.maxb.soulmate.swipe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.swipe.dto.MatchDto;
import ru.maxb.soulmate.swipe.mapper.MatchMapper;
import ru.maxb.soulmate.swipe.repository.MatchRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;

    public List<MatchDto> getMatches(UUID userId) {
        return matchRepository.findAllBySoulmateId(userId).stream()
                .map(matchMapper::toDto)
                .toList();
    }
}
