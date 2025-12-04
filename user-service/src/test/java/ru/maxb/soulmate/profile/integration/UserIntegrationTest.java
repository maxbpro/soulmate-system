package ru.maxb.soulmate.profile.integration;

import io.minio.errors.MinioException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import ru.maxb.soulmate.face.dto.FaceResponse;
import ru.maxb.soulmate.profile.common.AbstractPostgresqlTest;
import ru.maxb.soulmate.profile.service.FaceLandmarkService;
import ru.maxb.soulmate.profile.service.ObjectStorageService;
import ru.maxb.soulmate.profile.service.ProfileService;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Testcontainers
public class UserIntegrationTest extends AbstractPostgresqlTest {

    private static final int WIREMOCK_PORT = 8099;
    private static final int ELASTICSEARCH_PORT = 9200;
    private static final int MINIO_PORT = 9000;

    public static final WireMockContainer wireMockContainer = new WireMockContainer(DockerImageName.parse("wiremock/wiremock:3.13.0"))
            .withExposedPorts(WIREMOCK_PORT)
            .withReuse(true)
            .withMappingFromResource("face-service", "mappings/stubs.json")
            .withNetwork(network);

    public static MinIOContainer minIOContainer = new MinIOContainer(
            "minio/minio:RELEASE.2025-09-07T16-13-09Z")
            .withReuse(true)
            .withUserName("user")
            .withPassword("password")
            .withExposedPorts(MINIO_PORT)
            .withNetworkAliases("minio")
            .withNetwork(network);

    public static final ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer("elasticsearch:7.17.10")
                    .withExposedPorts(ELASTICSEARCH_PORT)
                    .withReuse(true)
                    .withEnv("xpack.security.enabled", "false")
                    .withPassword("password") //elastic
                    .withNetworkAliases("elasticsearch")
                    .withNetwork(network);

    @Autowired
    private ProfileService profileService;

    @Autowired
    private FaceLandmarkService faceLandmarkService;

    @Autowired
    private ObjectStorageService objectStorageService;


    @BeforeAll
    public static void setUp() {
        Startables.deepStart(Stream.of(elasticsearch, minIOContainer, wireMockContainer))
                .join();

        log.info("Elasticsearch server started on port {}", elasticsearch.getMappedPort(ELASTICSEARCH_PORT));
        log.info("minIO started on port {}", minIOContainer.getMappedPort(MINIO_PORT));
        log.info("WireMock server started on port {}", wireMockContainer.getMappedPort(WIREMOCK_PORT));
    }

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);

        dynamicPropertyRegistry.add("minio.endpoint", minIOContainer::getS3URL);
        dynamicPropertyRegistry.add("minio.accessKey", minIOContainer::getUserName);
        dynamicPropertyRegistry.add("minio.secretKey", minIOContainer::getPassword);

        final String wireMockFaceApiBase = "http://" + wireMockContainer.getHost() + ":"
                + wireMockContainer.getMappedPort(8080);

        dynamicPropertyRegistry.add("face.url", () -> wireMockFaceApiBase);

        dynamicPropertyRegistry.add("minio.secretKey", () -> wireMockFaceApiBase);
    }

    @Test
    void testContainerAreRunningWithNoExceptions() {
        assertThat(elasticsearch.isRunning()).isTrue();
        assertThat(minIOContainer.isRunning()).isTrue();
        assertThat(wireMockContainer.isRunning()).isTrue();
    }

    @Test
    void testRegistration() {

        var request = getProfileRegistrationRequestDto();

        ProfileDto registerProfileDto = profileService.register(request);

        assertThat(registerProfileDto).isNotNull();
    }

    private void getLandmarks() {

        String base64Image = getBase64Image();

        var landmarks = faceLandmarkService.getLandmarks(base64Image);

        assertThat(landmarks).isNotNull();

        String objectName = UUID.randomUUID().toString();

        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);

        try (InputStream inputStream = new ByteArrayInputStream(decodedBytes)) {
            objectStorageService.saveObject(objectName, inputStream, "image/jpeg");
        } catch (IOException | MinioException | NoSuchAlgorithmException | InvalidKeyException ex) {
            ex.printStackTrace();
        }

    }


    @SneakyThrows
    public String getBase64Image() {
        return Base64.getEncoder().encodeToString(
                new ClassPathResource("photo.jpeg").getContentAsByteArray());
    }

    @SneakyThrows
    public InputStream getImageInputStream() {
        return new ClassPathResource("photo.jpeg").getInputStream();
    }

    private ProfileRegistrationRequestDto getProfileRegistrationRequestDto() {
        var request = new ProfileRegistrationRequestDto();
        request.setAgeMax(99);
        request.setAgeMin(18);
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setEmail("john.smith@gmail.com");
        request.setPhoneNumber("1234567890");
        request.setRadius(10);
        request.setPhoto(getBase64Image());
        request.setBirthDate(LocalDate.of(1990, 11, 14));
        request.interestedIn(ProfileRegistrationRequestDto.InterestedInEnum.FEMALE);
        return request;
    }
}
