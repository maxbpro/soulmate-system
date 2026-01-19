package ru.maxb.soulmate.swipe.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;



@Slf4j
@SpringBootTest
public class AbstractCassandraTest {

    private static final int CASSANDRA_PORT = 9042;
    private static final String KEYSPACE_NAME = "swipe";

    public static final CassandraContainer<?> cassandra
            = new CassandraContainer<>("cassandra:3.11.2")
            .withExposedPorts(CASSANDRA_PORT)
            .withReuse(true)
            .withInitScript("init.cql")
            .withNetworkAliases("cassandra");

    @BeforeAll
    public static void beforeAll() {
        cassandra.start();
        int port = cassandra.getMappedPort(CASSANDRA_PORT);
        log.info("Cassandra server started on port {}", port);
    }

    @DynamicPropertySource
    static void cassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cassandra.keyspace-name", () -> KEYSPACE_NAME);
        registry.add("spring.cassandra.contact-points", cassandra::getContainerIpAddress);
        registry.add("spring.cassandra.port", () -> String.valueOf(cassandra.getMappedPort(CASSANDRA_PORT)));
        registry.add("spring.cassandra.local-datacenter", () -> "datacenter1");
    }
}
