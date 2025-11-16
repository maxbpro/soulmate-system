package ru.maxb.soulmate.profile.integration;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import ru.maxb.soulmate.face.dto.FaceResponse;
import ru.maxb.soulmate.profile.repository.ProfileRepository;
import ru.maxb.soulmate.profile.service.FaceLandmarkService;
import ru.maxb.soulmate.profile.service.ObjectStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
@SpringBootTest
public class MinioTest {

    private static final int MINIO_PORT = 9000;
    private static final Integer WIREMOCK_PORT = 8099;

    public static Network network = Network.newNetwork();

    public static MinIOContainer minIOContainer = new MinIOContainer(
            "minio/minio:RELEASE.2025-09-07T16-13-09Z")
            .withReuse(true)
            .withUserName("user")
            .withPassword("password")
            .withExposedPorts(MINIO_PORT)
            .withNetworkAliases("minio")
            .withNetwork(network);


    public static final WireMockContainer wireMockContainer = new WireMockContainer(DockerImageName.parse("wiremock/wiremock:3.13.0"))
            .withExposedPorts(WIREMOCK_PORT)
                .withReuse(true)
                .withMappingFromResource("face-service", "mappings/stubs.json");

    @MockitoBean
    private SpringLiquibase liquibase;

    @MockitoBean
    private EntityManagerFactory entityManagerFactory;

    @MockitoBean
    private ProfileRepository profileRepository;

    @Autowired
    private ObjectStorageService objectStorageService;

    @Autowired
    private FaceLandmarkService faceLandmarkService;

    @BeforeAll
    public static void setUp() {
        Startables.deepStart(Stream.of(minIOContainer, wireMockContainer))
                .join();

        log.info("minIO started on port {}", minIOContainer.getMappedPort(MINIO_PORT));
        log.info("WireMock server started on port {}", wireMockContainer.getMappedPort(WIREMOCK_PORT));
    }

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("minio.endpoint", minIOContainer::getS3URL);
        dynamicPropertyRegistry.add("minio.accessKey", minIOContainer::getUserName);
        dynamicPropertyRegistry.add("minio.secretKey", minIOContainer::getPassword);

        final String wireMockFaceApiBase = "http://" + wireMockContainer.getHost() + ":"
                + wireMockContainer.getFirstMappedPort();


        dynamicPropertyRegistry.add("minio.secretKey", () -> wireMockFaceApiBase);
    }

    @Test
    void testContainerAreRunningWithNoExceptions() {
        assertThat(minIOContainer.isRunning()).isTrue();
        assertThat(wireMockContainer.isRunning()).isTrue();
    }

    @Test
    @SneakyThrows
    void saveImage() {
        String objectName = UUID.randomUUID().toString();
        MultipartFile multipartFileFromResource = createMultipartFileFromResource();
        objectStorageService.saveObject(objectName + ".jpg", multipartFileFromResource);

        try (InputStream imageInputStream = objectStorageService.findObject(objectName + ".jpg")) {
            assertThat(imageInputStream).isNotNull();
        }

        FaceResponse landmarks = faceLandmarkService.getLandmarks(multipartFileFromResource);

        assertThat(landmarks).isNotNull();
    }

    public MultipartFile createMultipartFileFromResource() throws IOException {
        return new MockMultipartFile(
                "image",         // The name of the parameter in the multipart form (e.g., "file")
                "originalTest.txt",      // The original filename in the client's filesystem
                "image/jpeg",           // The content type of the file (e.g., "application/json", "image/jpeg")
                new ClassPathResource("photo.jpeg").getInputStream() // The content of the file as an InputStream
        );
    }
}
