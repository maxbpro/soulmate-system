package ru.maxb.soulmate.swipe.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import ru.maxb.soulmate.swipe.model.MatchEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends CassandraRepository<MatchEntity, String> {

    List<MatchEntity> findAllBySoulmateId(UUID soulmateId);
}
