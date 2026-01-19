package ru.maxb.soulmate.swipe.repository;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import org.springframework.data.cassandra.core.mapping.MapId;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import ru.maxb.soulmate.swipe.model.SwipeEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SwipeRepository extends CassandraRepository<SwipeEntity, MapId> {

    @Consistency(DefaultConsistencyLevel.LOCAL_QUORUM)
    @Query("SELECT liked FROM swipe WHERE userPair = ?0 AND userId = ?1 AND swipedUserId = ?2")
    Optional<Boolean> hasReverseSwipe(String userPair, UUID userId, UUID swipedUserId);

    Optional<SwipeEntity> findByUserPairAndUserIdAndSwipedUserId(String userPair, UUID userId, UUID swipedUserId);

    //for test, use with Caution
    @Query("SELECT * FROM swipe WHERE userId = ?0 ALLOW FILTERING")
    List<SwipeEntity> findByUserId(UUID userId);
}
