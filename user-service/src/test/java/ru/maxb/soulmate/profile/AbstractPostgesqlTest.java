package ru.maxb.soulmate.profile;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


@Slf4j
@SpringBootTest
public class AbstractPostgesqlTest {

    private static final int POSTGRES_PORT = 5432;
    private static final String POSTGRES_DATABASE_NAME = "profileDatabase";

    private static PostgreSQLContainer postgresqlContainer =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withExposedPorts(POSTGRES_PORT)
                    .withDatabaseName(POSTGRES_DATABASE_NAME)
                    .withUsername("customer")
                    .withPassword("password")
                    .withReuse(true)
                    .withNetworkAliases("postgres");

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

}
