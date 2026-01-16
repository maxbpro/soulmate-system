package ru.maxb.soulmate.profile.integration;

import com.jayway.jsonpath.JsonPath;
import io.debezium.testing.testcontainers.ConnectorConfiguration;
import io.debezium.testing.testcontainers.DebeziumContainer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import ru.maxb.soulmate.profile.common.AbstractPostgresqlTest;
import ru.maxb.soulmate.profile.model.OutboxType;
import ru.maxb.soulmate.profile.service.FaceLandmarkService;
import ru.maxb.soulmate.profile.service.ObjectStorageService;
import ru.maxb.soulmate.profile.service.ProfileService;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@Testcontainers
public class UserIntegrationTest extends AbstractPostgresqlTest {

    private static final int WIREMOCK_PORT = 8099;
    private static final int MINIO_PORT = 9000;
    private static final int KAFKA_PORT = 9092;
    private static final int DEBEZIUM_PORT = 8083;

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

    public static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0")
            .withExposedPorts(KAFKA_PORT)
            .withNetworkAliases("kafka")
            .withNetwork(network)
            .withReuse(true)
            .withListener("kafka:19092");

    public static DebeziumContainer debeziumContainer =
            new DebeziumContainer("quay.io/debezium/connect:3.3.1.Final")
                    .withExposedPorts(DEBEZIUM_PORT)
                    .withNetwork(network)
                    .withKafka(network, "kafka:19092")
                    .dependsOn(kafka)
                    .withReuse(true);

    @Autowired
    private ProfileService profileService;

    @Autowired
    private FaceLandmarkService faceLandmarkService;

    @Autowired
    private ObjectStorageService objectStorageService;

    @BeforeAll
    public static void setUp() {
        Startables.deepStart(Stream.of(minIOContainer, wireMockContainer, kafka,
                debeziumContainer)).join();

        log.info("Kafka started on port {}", kafka.getMappedPort(KAFKA_PORT));
        log.info("Debezium started on port {}", debeziumContainer.getMappedPort(DEBEZIUM_PORT));
        log.info("minIO started on port {}", minIOContainer.getMappedPort(MINIO_PORT));
        log.info("WireMock server started on port {}", wireMockContainer.getMappedPort(WIREMOCK_PORT));
    }

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("minio.endpoint", minIOContainer::getS3URL);
        dynamicPropertyRegistry.add("minio.accessKey", minIOContainer::getUserName);
        dynamicPropertyRegistry.add("minio.secretKey", minIOContainer::getPassword);

        final String wireMockFaceApiBase = "http://" + wireMockContainer.getHost() + ":"
                + wireMockContainer.getMappedPort(8080);

        dynamicPropertyRegistry.add("face.url", () -> wireMockFaceApiBase);
    }

    @Test
    void testContainerAreRunning() {
        assertThat(kafka.isRunning()).isTrue();
        assertThat(debeziumContainer.isRunning()).isTrue();
        assertThat(wireMockContainer.isRunning()).isTrue();
        assertThat(wireMockContainer.isRunning()).isTrue();
    }

    @Test
    void testRegistration() {
        var request = getProfileRegistrationRequestDto();

        ProfileDto registerProfileDto = profileService.register(request);

        assertThat(registerProfileDto).isNotNull();

        checkKafka();
    }

    @Test
    void testPhotoUpload() throws IOException {
        var request = getProfileRegistrationRequestDto();
        ProfileDto registerProfileDto = profileService.register(request);
        UUID profileId = UUID.fromString(Objects.requireNonNull(registerProfileDto.getId()));

        profileService.uploadImage(profileId, createMockImage());
        ProfileDto profileDto = profileService.findById(profileId);

        assertEquals(1, profileDto.getPhotos().size());

        UUID savedPhotoId = profileDto.getPhotos().stream().findFirst()
                .map(UUID::fromString)
                .orElseThrow();

        profileService.deleteImage(profileId, savedPhotoId);
        profileDto = profileService.findById(profileId);

        assertEquals(0, profileDto.getPhotos().size());
    }

    private void checkKafka() {
        String schema = "profile";
        String table = "outbox";
        String topic = String.format("dbserver.%s.%s", schema, table);
        debeziumContainer.registerConnector("profile-connector", getTestConnectorConfiguration());

        try (KafkaConsumer<String, String> consumer = getConsumer(kafka)) {
            consumer.subscribe(List.of(topic));

            List<ConsumerRecord<String, String>> changeEvents = drain(consumer, 1);

            ConsumerRecord<String, String> stringStringConsumerRecord = changeEvents.get(0);

            assertThat(stringStringConsumerRecord.topic()).isEqualTo(topic);
            assertThat(JsonPath.<String>read(changeEvents.get(0).key(), "$.id")).isNotBlank();
            assertThat(JsonPath.<String>read(changeEvents.get(0).key(), "$.id")).isNotBlank();

            String value = changeEvents.get(0).value();
            assertThat(JsonPath.<String>read(value, "$.after.aggregatetype")).isEqualTo("ProfileEntity");
            assertThat(JsonPath.<String>read(value, "$.after.type")).isEqualTo(OutboxType.PROFILE_CREATED.toString());

            consumer.unsubscribe();
        }
    }

    private ConnectorConfiguration getTestConnectorConfiguration() {
        return ConnectorConfiguration
                .forJdbcContainer(postgresqlContainer)
                .with("plugin.name", "pgoutput")
                .with("topic.prefix", "dbserver");
    }

    private KafkaConsumer<String, String> getConsumer(KafkaContainer kafkaContainer) {
        return new KafkaConsumer<>(
                Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                        ConsumerConfig.GROUP_ID_CONFIG, "tc-" + UUID.randomUUID(),
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        );
    }

    private List<ConsumerRecord<String, String>> drain(
            KafkaConsumer<String, String> consumer,
            int expectedRecordCount) {

        List<ConsumerRecord<String, String>> allRecords = new ArrayList<>();

        Unreliables.retryUntilTrue(10, TimeUnit.SECONDS, () -> {

            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(50));

            consumerRecords
                    .iterator()
                    .forEachRemaining(allRecords::add);

            return allRecords.size() == expectedRecordCount;
        });

        return allRecords;
    }

    private ProfileRegistrationRequestDto getProfileRegistrationRequestDto() {
        var request = new ProfileRegistrationRequestDto();
        request.setPrincipalId(UUID.randomUUID());
        request.setAgeMin(18);
        request.setAgeMax(99);
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setEmail("john.smith@gmail.com");
        request.setPhoneNumber("1234567890");
        request.setRadius(10);
        request.setPhoto(getBase64Image());
        request.setBirthDate(LocalDate.of(1990, 11, 14));
        request.interestedIn(ProfileRegistrationRequestDto.InterestedInEnum.FEMALE);
        request.setGender(ProfileRegistrationRequestDto.GenderEnum.MALE);
        return request;
    }

    @SneakyThrows
    public String getBase64Image() {
        return Base64.getEncoder().encodeToString(
                new ClassPathResource("photo.jpeg").getContentAsByteArray());
    }

    public static MockMultipartFile createMockImage() throws IOException {
//        Path path = Paths.get("src/test/resources/sample-image.png");
//        String originalFileName = "sample-image.png";
//        String contentType = MediaType.IMAGE_JPEG_VALUE;
//
//        byte[] content = Files.readAllBytes(path);
        byte[] contentAsByteArray = new ClassPathResource("photo.jpeg").getContentAsByteArray();

        return new MockMultipartFile(
                "file",
                "photo.jpeg",
                MediaType.IMAGE_JPEG_VALUE,
                contentAsByteArray
        );
    }
}
