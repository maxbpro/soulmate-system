package ru.maxb.soulmate.landmark.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import ru.maxb.soulmate.landmark.repository.LandmarkMatchRepository;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
public class AbstractPostgresqlTest {

    private static final int POSTGRES_PORT = 5432;
    private static final String POSTGRES_DATABASE_NAME = "recommendationDatabase";

    private static PostgreSQLContainer postgresqlContainer =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withExposedPorts(POSTGRES_PORT)
                    .withDatabaseName(POSTGRES_DATABASE_NAME)
                    .withUsername("customer")
                    .withPassword("password")
                    .withReuse(true)
                    .withNetworkAliases("postgres");

    @MockitoBean
    private LandmarkMatchRepository landmarkMatchRepository;


    @BeforeAll
    public static void setUp() {
        postgresqlContainer.setWaitStrategy(
                new LogMessageWaitStrategy()
                        .withRegEx(".*database system is ready to accept connections.*\\s")
                        .withTimes(1)
                        .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS))
        );
        postgresqlContainer.start();
        int port = postgresqlContainer.getMappedPort(POSTGRES_PORT);
        log.info("Postgres server started on port {}", port);
    }


    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", postgresqlContainer::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", postgresqlContainer::getPassword);
        dynamicPropertyRegistry.add("spring.datasource.driver-class-name", postgresqlContainer::getDriverClassName);
    }

    @Test
    void givenPostgresqlContainer_whenSpringContextIsBootstrapped_thenContainerIsRunningWithNoExceptions() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }
}
