package ru.maxb.soulmate.gateway.containers;

import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

public class WireMockTestContainer {

    public static final int WIREMOCK_PORT = 8080;
    public static final WireMockContainer wireMockContainer;

    static {
        wireMockContainer = new WireMockContainer(DockerImageName.parse("wiremock/wiremock:3.13.0"))
                .withExposedPorts(WIREMOCK_PORT)
                .withReuse(true)
                .withMappingFromResource("user-service", "mappings/stubs.json");
    }
}
