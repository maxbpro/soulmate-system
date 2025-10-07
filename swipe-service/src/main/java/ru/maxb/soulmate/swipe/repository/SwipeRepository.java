package ru.maxb.soulmate.swipe.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import ru.maxb.soulmate.swipe.model.SwipeEntity;

@Repository
public interface SwipeRepository extends CassandraRepository<SwipeEntity, String> {
}
