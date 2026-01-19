-- init.cql
CREATE KEYSPACE IF NOT EXISTS swipe WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};
USE swipe;
CREATE TABLE IF NOT EXISTS swipe
(
    userPair     text, -- partition key: smaller_id:larger_id
    userId       uuid, -- clustering key
    swipedUserId uuid, -- clustering key
    liked        boolean,
    createdAt    timestamp,
    PRIMARY KEY ((userPair), userId, swipedUserId)
);

CREATE TABLE IF NOT EXISTS match
(
    userPair   text PRIMARY KEY, -- partition key
    user1Id    uuid,
    user2Id    uuid,
    createdAt  timestamp
);
