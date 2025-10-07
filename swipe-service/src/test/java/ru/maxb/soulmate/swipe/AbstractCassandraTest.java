package ru.maxb.soulmate.swipe;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;

import static org.assertj.core.api.Assertions.assertThat;


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

    @Test
    void givenCassandraContainer_whenSpringContextIsBootstrapped_thenContainerIsRunningWithNoExceptions() {
        assertThat(cassandra.isRunning()).isTrue();
    }

    @BeforeAll
    public static void setUp() {
        cassandra.start();
        int port = cassandra.getMappedPort(CASSANDRA_PORT);
        log.info("Cassandra server started on port {}", port);
    }

    @DynamicPropertySource
    static void cassandraProperties(DynamicPropertyRegistry registry) {
        System.setProperty("spring.cassandra.keyspace-name", KEYSPACE_NAME);
        System.setProperty("spring.cassandra.contact-points", cassandra.getContainerIpAddress());
        System.setProperty("spring.cassandra.port", String.valueOf(cassandra.getMappedPort(CASSANDRA_PORT)));
    }

}
