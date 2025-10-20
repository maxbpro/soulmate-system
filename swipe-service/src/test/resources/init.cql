-- init.cql
CREATE KEYSPACE IF NOT EXISTS swipe WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};
USE swipe;
CREATE TABLE IF NOT EXISTS swipe
(
    id           text PRIMARY KEY,
    userId       uuid,
    swipedUserId uuid,
    liked        boolean
)
            WITH cdc = true;