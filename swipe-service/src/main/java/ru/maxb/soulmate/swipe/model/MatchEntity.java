package ru.maxb.soulmate.swipe.model;

import lombok.*;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "match", keyspace = "swipe")
public class MatchEntity {

    @PrimaryKeyColumn(
            type = PrimaryKeyType.PARTITIONED,
            ordinal = 0
    )
    private String userPair;  // e.g., "A:B"

    private UUID user1Id;

    private UUID user2Id;

    private Instant createdAt;

    // Add secondary indexes
    @Indexed
    public UUID getUser1Id() { return user1Id; }

    @Indexed
    public UUID getUser2Id() { return user2Id; }
}
