package ru.maxb.soulmate.profile.integration;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import ru.maxb.soulmate.profile.repository.ProfileRepository;
import ru.maxb.soulmate.profile.service.ObjectStorageService;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
@SpringBootTest
public class MinioTest {

    private static final int MINIO_PORT = 9000;

    public static Network network = Network.newNetwork();

    public static MinIOContainer minIOContainer = new MinIOContainer(
            "minio/minio:RELEASE.2025-09-07T16-13-09Z")
            .withReuse(true)
            .withUserName("user")
            .withPassword("password")
            .withExposedPorts(MINIO_PORT)
            .withNetworkAliases("minio")
            .withNetwork(network);

    @MockitoBean
    private SpringLiquibase liquibase;

    @MockitoBean
    private EntityManagerFactory entityManagerFactory;

    @MockitoBean
    private ProfileRepository profileRepository;

    @Autowired
    private ObjectStorageService objectStorageService;

    @BeforeAll
    public static void setUp() {
        Startables.deepStart(Stream.of(minIOContainer))
                .join();

        log.info("minIO started on port {}", minIOContainer.getMappedPort(MINIO_PORT));
    }

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("minio.endoint", minIOContainer::getS3URL);
        dynamicPropertyRegistry.add("minio.accessKey", minIOContainer::getUserName);
        dynamicPropertyRegistry.add("minio.secretKey", minIOContainer::getPassword);
    }

    @Test
    void testContainerAreRunningWithNoExceptions() {
        assertThat(minIOContainer.isRunning()).isTrue();
    }

    @Test
    void saveImage() {
        objectStorageService.saveObject("testIamge", null);
    }
}
