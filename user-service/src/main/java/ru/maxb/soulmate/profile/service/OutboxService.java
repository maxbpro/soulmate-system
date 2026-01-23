package ru.maxb.soulmate.profile.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.maxb.soulmate.profile.exception.ProfileException;
import ru.maxb.soulmate.profile.model.OutboxType;
import ru.maxb.soulmate.profile.model.OutboxEntity;
import ru.maxb.soulmate.profile.repository.OutboxRepository;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(String aggregateId, String aggregateType, JsonNode payload, OutboxType type) {
        OutboxEntity outboxEntity = new OutboxEntity();
        outboxEntity.setAggregateType(aggregateType);
        outboxEntity.setAggregateId(aggregateId);
        outboxEntity.setType(type);
        outboxEntity.setPayload(payload);

        outboxRepository.save(outboxEntity);
        log.debug("Saved outbox event: {} for aggregate: {}", type, aggregateId);
    }

    @Transactional
    public void hardDelete(UUID aggregateId, OutboxType outboxType) {
        var outbox = outboxRepository.findByAggregateIdAndType(String.valueOf(aggregateId), outboxType)
                .orElseThrow(() -> new ProfileException("Outbox Profile not found by id=[%s] and outboxType=[%s]", aggregateId, outboxType));
        outboxRepository.delete(outbox);
        log.info("Deleted outbox event: {} for aggregate: {}", outboxType, aggregateId);
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    @Transactional
    public void cleanupOldOutboxRecords() {
        // Keep records for a reasonable time (e.g., 24 hours)
        // to handle any CDC delays or reprocessing needs
        Instant cutoffTime = Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS);

        int deletedCount = outboxRepository.deleteByCreatedBefore(cutoffTime);

        if (deletedCount > 0) {
            log.info("Cleaned up {} old outbox records", deletedCount);
        }
    }
}
