package ru.maxb.soulmate.swipe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.swipe.model.MatchEntity;
import ru.maxb.soulmate.swipe.repository.MatchRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    public List<MatchEntity> getMatches(UUID userId) {
        return matchRepository.findAllBySoulmateId(userId).stream()
                .toList();
    }
}
