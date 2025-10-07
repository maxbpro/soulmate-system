package ru.maxb.soulmate.swipe.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;


@Getter
@Setter
@Table(value = "swipe", keyspace = "swipe")
public class SwipeEntity {

    @PrimaryKey
    private String id;

    private UUID userId;

    private UUID swipedUserId;

    private Boolean liked;
}
