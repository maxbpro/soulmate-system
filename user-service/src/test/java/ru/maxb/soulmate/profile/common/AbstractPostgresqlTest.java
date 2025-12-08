package ru.maxb.soulmate.profile.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
public class AbstractPostgresqlTest {

    private static final int POSTGRES_PORT = 5432;
    private static final String POSTGRES_DATABASE_NAME = "profileDatabase";

    protected static Network network = Network.newNetwork();

    protected static PostgreSQLContainer postgresqlContainer =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withExposedPorts(POSTGRES_PORT)
                    .withDatabaseName(POSTGRES_DATABASE_NAME)
                    .withUsername("customer")
                    .withPassword("password")
                    .withReuse(true)
                    .withNetwork(network)
                    .withNetworkAliases("postgres")
                    .withEnv("POSTGRES_INITDB_ARGS", "-c wal_level=logical");

    @BeforeAll
    public static void setUp() {
        Startables.deepStart(Stream.of(postgresqlContainer))
                .join();

        log.info("Postgres server started on port {}", postgresqlContainer.getMappedPort(POSTGRES_PORT));
    }

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", postgresqlContainer::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", postgresqlContainer::getPassword);
        dynamicPropertyRegistry.add("spring.datasource.driver-class-name", postgresqlContainer::getDriverClassName);
    }

    @Test
    void givenContainers_whenSpringContextIsBootstrapped_thenContainerIsRunningWithNoExceptions() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }
}
