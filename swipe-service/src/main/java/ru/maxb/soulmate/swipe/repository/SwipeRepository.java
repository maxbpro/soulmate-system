package ru.maxb.soulmate.swipe.repository;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import ru.maxb.soulmate.swipe.model.SwipeEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SwipeRepository extends CassandraRepository<SwipeEntity, MapId> {


    @Consistency(DefaultConsistencyLevel.LOCAL_QUORUM)
    @Query("SELECT liked FROM swipe WHERE userPair = ?0 AND userId = ?1 AND swipedUserId = ?2")
    Optional<Boolean> hasMatch(String userPair, UUID userId, UUID swipedUserId);

}
