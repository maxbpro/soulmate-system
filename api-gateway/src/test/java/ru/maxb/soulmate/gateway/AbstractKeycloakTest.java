package ru.maxb.soulmate.gateway;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import ru.maxb.soulmate.gateway.config.AppTestConfig;
import ru.maxb.soulmate.gateway.service.GatewayApiTestService;
import ru.maxb.soulmate.gateway.service.KeycloakApiTestService;

import static ru.maxb.soulmate.gateway.containers.KeycloakTestContainer.KEYCLOAK_PORT;
import static ru.maxb.soulmate.gateway.containers.KeycloakTestContainer.keycloakTestContainer;
import static ru.maxb.soulmate.gateway.containers.PostgresTestContainer.POSTGRES_PORT;
import static ru.maxb.soulmate.gateway.containers.PostgresTestContainer.postgresTestContainer;
import static ru.maxb.soulmate.gateway.containers.WireMockTestContainer.WIREMOCK_PORT;
import static ru.maxb.soulmate.gateway.containers.WireMockTestContainer.wireMockContainer;

@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {ApiGatewayApplication.class,
                AppTestConfig.class,
                GatewayApiTestService.class,
                KeycloakApiTestService.class
                //TestSupportConfig.class
        })

public class AbstractKeycloakTest {

    public static PostgreSQLContainer postgres = postgresTestContainer;
    public static GenericContainer keycloak = keycloakTestContainer;
    public static WireMockContainer wireMockServer = wireMockContainer;

    static {
        postgres.start();
        int postgresPort = postgres.getMappedPort(POSTGRES_PORT);
        log.info("Postgres server started on port {}", postgresPort);

        keycloak.dependsOn(postgres);
        keycloak.start();

        int keycloakPort = keycloak.getMappedPort(KEYCLOAK_PORT);
        log.info("Keycloak server started on port {}", keycloakPort);

        wireMockServer.start();

        int wireMockPort = wireMockServer.getMappedPort(WIREMOCK_PORT);
        log.info("WireMock server started on port {}", wireMockPort);
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        final String kcBase = "http://" +
                keycloak.getHost() + ":" +
                keycloak.getFirstMappedPort();

        final String wireMockBase = "http://" +
                wireMockServer.getHost() + ":" +
                wireMockServer.getFirstMappedPort();

        r.add("user.url", () -> wireMockBase);

        r.add("application.keycloak.serverUrl", () -> kcBase);
        r.add("application.keycloak.realm", () -> "individual");
        r.add("application.keycloak.clientId", () -> "individual");
        r.add("application.keycloak.clientSecret", () -> "FaxzBgk7pkyattBrV8MlVCVg80jjZKo5");
        r.add("application.keycloak.adminClientId", () -> "admin-cli");
        r.add("application.keycloak.adminUsername", () -> "admin");
        r.add("application.keycloak.adminPassword", () -> "admin");

        r.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> kcBase + "/realms/individual");

        r.add("auth.url", () -> kcBase);
        r.add("users.url", () -> kcBase);
        r.add("profile.url", () -> wireMockBase);
    }


}
