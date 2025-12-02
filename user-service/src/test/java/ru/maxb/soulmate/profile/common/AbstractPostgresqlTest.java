package ru.maxb.soulmate.profile.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.lifecycle.Startables;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
public class AbstractPostgresqlTest {

    private static final int MINIO_PORT = 9000;
    private static final int POSTGRES_PORT = 5432;
    private static final String POSTGRES_DATABASE_NAME = "profileDatabase";

    public static Network network = Network.newNetwork();

    private static PostgreSQLContainer postgresqlContainer =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withExposedPorts(POSTGRES_PORT)
                    .withDatabaseName(POSTGRES_DATABASE_NAME)
                    .withUsername("customer")
                    .withPassword("password")
                    .withReuse(true)
                    .withNetworkAliases("postgres");

    public static MinIOContainer minIOContainer = new MinIOContainer(
            "minio/minio:RELEASE.2025-09-07T16-13-09Z")
            .withReuse(true)
            .withUserName("user")
            .withPassword("password")
            .withExposedPorts(MINIO_PORT)
            .withNetworkAliases("minio")
            .withNetwork(network);


    @BeforeAll
    public static void setUp() {
        Startables.deepStart(Stream.of(minIOContainer, postgresqlContainer))
                .join();

        log.info("minIO started on port {}", minIOContainer.getMappedPort(MINIO_PORT));
        log.info("Postgres server started on port {}", postgresqlContainer.getMappedPort(POSTGRES_PORT));
    }

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", postgresqlContainer::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", postgresqlContainer::getPassword);
        dynamicPropertyRegistry.add("spring.datasource.driver-class-name", postgresqlContainer::getDriverClassName);
        dynamicPropertyRegistry.add("minio.endpoint", minIOContainer::getS3URL);
        dynamicPropertyRegistry.add("minio.accessKey", minIOContainer::getUserName);
        dynamicPropertyRegistry.add("minio.secretKey", minIOContainer::getPassword);
    }

    @Test
    void givenContainers_whenSpringContextIsBootstrapped_thenContainerIsRunningWithNoExceptions() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
        assertThat(minIOContainer.isRunning()).isTrue();
    }
}
