package ru.maxb.soulmate.profile.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.profile.exception.ProfileException;
import ru.maxb.soulmate.profile.model.OutboxType;
import ru.maxb.soulmate.profile.model.OutboxEntity;
import ru.maxb.soulmate.profile.repository.OutboxRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;

    @Transactional
    public void save(String aggregateId, String aggregateType, JsonNode payload, OutboxType type) {
        OutboxEntity outboxEntity = new OutboxEntity();
        outboxEntity.setAggregateType(aggregateType);
        outboxEntity.setAggregateId(aggregateId);
        outboxEntity.setType(type);
        outboxEntity.setPayload(payload);
        outboxRepository.save(outboxEntity);
    }

    @Transactional
    public void hardDelete(UUID aggregateId, OutboxType outboxType) {
        var outbox = outboxRepository.findByAggregateIdAndType(String.valueOf(aggregateId), outboxType)
                .orElseThrow(() -> new ProfileException("Outbox Profile not found by id=[%s] and outboxType=[%s]", aggregateId, outboxType));
        log.info("IN - hardDelete: outbox profile with id = [{}] successfully deleted", aggregateId);
        outboxRepository.delete(outbox);
    }
}
