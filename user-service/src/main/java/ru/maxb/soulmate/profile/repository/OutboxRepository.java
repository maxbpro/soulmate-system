package ru.maxb.soulmate.profile.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.maxb.soulmate.profile.model.OutboxType;
import ru.maxb.soulmate.profile.model.OutboxEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OutboxRepository extends CrudRepository<OutboxEntity, UUID> {

    Optional<OutboxEntity> findByAggregateIdAndType(String uuid, OutboxType outboxType);

    int deleteByCreatedBefore(Instant createdAt);

    List<OutboxEntity> findAll();
}
