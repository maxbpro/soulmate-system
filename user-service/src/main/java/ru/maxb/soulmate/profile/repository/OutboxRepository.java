package ru.maxb.soulmate.profile.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.maxb.soulmate.profile.model.OutboxEntity;

import java.util.UUID;

@Repository
public interface OutboxRepository extends CrudRepository<OutboxEntity, UUID> {
}
