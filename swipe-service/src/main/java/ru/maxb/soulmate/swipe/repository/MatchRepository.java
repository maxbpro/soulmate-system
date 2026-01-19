package ru.maxb.soulmate.swipe.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;
import ru.maxb.soulmate.swipe.model.MatchEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends CassandraRepository<MatchEntity, String> {

    @Query("SELECT * FROM match WHERE user1id = ?0 ALLOW FILTERING")
    List<MatchEntity> findAllByUser1Id(UUID userId);

    @Query("SELECT * FROM match WHERE user2id = ?0 ALLOW FILTERING")
    List<MatchEntity> findAllByUser2Id(UUID userId);

    default List<MatchEntity> findAllInvolvingUser(UUID userId) {
        List<MatchEntity> matches = new ArrayList<>();
        matches.addAll(findAllByUser1Id(userId));
        matches.addAll(findAllByUser2Id(userId));
        return matches;
    }

    @Query("SELECT * FROM match WHERE userPair = ?0 LIMIT 1")
    Optional<MatchEntity> findByUserPair(String userPair);

    default boolean existsByUserPair(String userPair) {
        return findByUserPair(userPair).isPresent();
    }
}
