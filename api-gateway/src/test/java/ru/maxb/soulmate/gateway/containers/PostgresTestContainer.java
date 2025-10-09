package ru.maxb.soulmate.gateway.containers;

import org.testcontainers.containers.PostgreSQLContainer;
import ru.maxb.soulmate.gateway.util.Setting;

public class PostgresTestContainer {

    public static final int POSTGRES_PORT = 5432;
    public static final PostgreSQLContainer postgresTestContainer;

    static {
        postgresTestContainer = new PostgreSQLContainer<>("postgres:18-alpine")
                .withExposedPorts(POSTGRES_PORT)
                .withDatabaseName("keycloak")
                .withUsername("keycloak")
                .withPassword("keycloak")
                .withReuse(true)
                .withNetwork(Setting.GLOBAL_NETWORK)
                .withNetworkAliases("postgres");
    }
}
