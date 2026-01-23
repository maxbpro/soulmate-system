package ru.maxb.soulmate.profile.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
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
import ru.maxb.soulmate.profile.exception.ProfileException;
import ru.maxb.soulmate.profile.model.OutboxEntity;
import ru.maxb.soulmate.profile.model.OutboxType;
import ru.maxb.soulmate.profile.repository.OutboxRepository;
import ru.maxb.soulmate.profile.repository.ProfileRepository;
import ru.maxb.soulmate.profile.service.FaceLandmarkService;
import ru.maxb.soulmate.profile.service.ObjectStorageService;
import ru.maxb.soulmate.profile.service.OutboxService;
import ru.maxb.soulmate.profile.service.ProfileService;
import ru.maxb.soulmate.user.dto.ProfileDto;
import ru.maxb.soulmate.user.dto.ProfileRegistrationRequestDto;
import ru.maxb.soulmate.user.dto.ProfileUpdateRequestDto;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String email;
    private static String topic;

    @BeforeAll
    public static void setUp() {
        Startables.deepStart(Stream.of(minIOContainer, wireMockContainer, kafka,
                debeziumContainer)).join();

        log.info("Kafka started on port {}", kafka.getMappedPort(KAFKA_PORT));
        log.info("Debezium started on port {}", debeziumContainer.getMappedPort(DEBEZIUM_PORT));
        log.info("minIO started on port {}", minIOContainer.getMappedPort(MINIO_PORT));
        log.info("WireMock server started on port {}", wireMockContainer.getMappedPort(WIREMOCK_PORT));

        String schema = "profile";
        String table = "outbox";
        topic = String.format("dbserver.%s.%s", schema, table);
        debeziumContainer.registerConnector("profile-connector", getTestConnectorConfiguration());
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

    @BeforeEach
    void cleanKafkaTopics() {
        // Clean up Kafka topics before each test
        try (KafkaConsumer<String, String> consumer = getConsumer()) {
            consumer.subscribe(List.of("dbserver.profile.outbox"));
            consumer.poll(Duration.ofMillis(100));
            consumer.seekToBeginning(consumer.assignment());
            consumer.commitSync();
        }

        email = "john.smith" + UUID.randomUUID() + "@gmail.com";
        profileRepository.deleteAll();
        outboxRepository.deleteAll();
    }

    @Test
    @Order(1)
    void testContainerAreRunning() {
        assertThat(kafka.isRunning()).isTrue();
        assertThat(debeziumContainer.isRunning()).isTrue();
        assertThat(wireMockContainer.isRunning()).isTrue();
        assertThat(wireMockContainer.isRunning()).isTrue();
    }


    @Test
    void testRegistration() {
        var request = getProfileRegistrationRequestDto(email);

        ProfileDto registerProfileDto = profileService.register(request);

        assertThat(registerProfileDto).isNotNull();
        assertThat(registerProfileDto.getId()).isNotBlank();
        assertThat(registerProfileDto.getEmail()).isEqualTo(email);
        assertThat(registerProfileDto.getFirstName()).isEqualTo("John");
        assertThat(registerProfileDto.getLastName()).isEqualTo("Smith");
        assertThat(registerProfileDto.getAgeMin()).isEqualTo(18);
        assertThat(registerProfileDto.getAgeMax()).isEqualTo(99);

        // Verify outbox event was created
        checkKafkaOutboxEvent(OutboxType.PROFILE_CREATED, registerProfileDto.getId());


        Iterable<OutboxEntity> all = outboxRepository.findAll();
        Optional<OutboxEntity> byAggregateIdAndType = outboxRepository.findByAggregateIdAndType(registerProfileDto.getId(), OutboxType.PROFILE_CREATED);

        assertThat(byAggregateIdAndType.isPresent()).isFalse();
    }

    @Test
    void testDuplicateRegistrationShouldFail() {
        var request = getProfileRegistrationRequestDto(email);
        UUID principalId = UUID.randomUUID();
        request.setPrincipalId(principalId);
        request.setEmail("duplicate1@gmail.com");
        profileService.register(request);

        // Second registration with same principalId should fail
        var duplicateRequest = getProfileRegistrationRequestDto(email);
        duplicateRequest.setPrincipalId(principalId); // Same principalId
        duplicateRequest.setEmail("duplicate2@gmail.com"); // Different email

        assertThatThrownBy(() -> profileService.register(duplicateRequest))
                .isInstanceOf(ProfileException.class)
                .hasMessageContaining("PrincipalId already registered: duplicate2@gmail.com");
    }

    @Test
    void testFindProfileById() {
        var request = getProfileRegistrationRequestDto(email);
        ProfileDto createdProfile = profileService.register(request);
        UUID profileId = UUID.fromString(createdProfile.getId());

        // Find the profile
        ProfileDto foundProfile = profileService.findById(profileId);

        assertThat(foundProfile).isNotNull();
        assertThat(foundProfile.getId()).isEqualTo(profileId.toString());
        assertThat(foundProfile.getEmail()).isEqualTo(createdProfile.getEmail());
    }

    @Test
    void testUpdateProfile() {
        var request = getProfileRegistrationRequestDto(email);
        ProfileDto createdProfile = profileService.register(request);
        UUID profileId = UUID.fromString(createdProfile.getId());

        // Update the profile
        ProfileUpdateRequestDto updateRequest = new ProfileUpdateRequestDto();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Doe");
        updateRequest.setAgeMin(21);
        updateRequest.setAgeMax(45);
        updateRequest.setRadius(50);

        ProfileDto updatedProfile = profileService.update(profileId, updateRequest);

        assertThat(updatedProfile.getFirstName()).isEqualTo("Jane");
        assertThat(updatedProfile.getLastName()).isEqualTo("Doe");
        assertThat(updatedProfile.getAgeMin()).isEqualTo(21);
        assertThat(updatedProfile.getAgeMax()).isEqualTo(45);
        assertThat(updatedProfile.getRadius()).isEqualTo(50);

        // Verify outbox event was created
        //checkKafkaOutboxEvent(OutboxType.PROFILE_UPDATED, profileId.toString());
    }

    @Test
    void testPhotoUpload() throws IOException {
        var request = getProfileRegistrationRequestDto(email);
        ProfileDto createdProfile = profileService.register(request);
        UUID profileId = UUID.fromString(createdProfile.getId());

        // Test single photo upload
        profileService.uploadImage(profileId, createMockImage());
        ProfileDto profileDto = profileService.findById(profileId);

        assertEquals(1, profileDto.getPhotos().size());

        UUID photoId = profileDto.getPhotos().stream().findFirst()
                .map(UUID::fromString)
                .orElseThrow();

        // Test multiple photo uploads
        profileService.uploadImage(profileId, createMockImage("photo2.jpeg"));
        profileDto = profileService.findById(profileId);
        assertEquals(2, profileDto.getPhotos().size());

        // Clean up
        profileService.deleteImage(profileId, photoId);
    }

    @Test
    void testPhotoUploadWithInvalidFile() throws IOException {
        var request = getProfileRegistrationRequestDto(email);
        ProfileDto createdProfile = profileService.register(request);
        UUID profileId = UUID.fromString(createdProfile.getId());

        // Test with file too large
        byte[] largeFile = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeImage = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeFile
        );

        assertThatThrownBy(() -> profileService.uploadImage(profileId, largeImage))
                .isInstanceOf(ProfileException.class)
                .hasMessageContaining("File size exceeds limit");

        // Test with invalid content type
        MockMultipartFile invalidType = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        assertThatThrownBy(() -> profileService.uploadImage(profileId, invalidType))
                .isInstanceOf(ProfileException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    void testDeletePhoto() throws IOException {
        var request = getProfileRegistrationRequestDto(email);
        ProfileDto createdProfile = profileService.register(request);
        UUID profileId = UUID.fromString(createdProfile.getId());

        // Upload a photo
        profileService.uploadImage(profileId, createMockImage());
        ProfileDto profileDto = profileService.findById(profileId);

        UUID photoId = profileDto.getPhotos().stream().findFirst()
                .map(UUID::fromString)
                .orElseThrow();

        // Delete the photo
        profileService.deleteImage(profileId, photoId);
        ProfileDto updatedProfile = profileService.findById(profileId);

        assertEquals(0, updatedProfile.getPhotos().size());
        assertFalse(updatedProfile.getPhotos().contains(photoId.toString()));
    }

    @Test
    void testDeleteNonExistentPhotoShouldFail() {
        var request = getProfileRegistrationRequestDto(email);
        ProfileDto createdProfile = profileService.register(request);
        UUID profileId = UUID.fromString(createdProfile.getId());

        UUID nonExistentPhotoId = UUID.randomUUID();

        assertThatThrownBy(() -> profileService.deleteImage(profileId, nonExistentPhotoId))
                .isInstanceOf(ProfileException.class)
                .hasMessageContaining("doesn't have photo");
    }

    @Test
    void testSoftDeleteProfile() {
        var request = getProfileRegistrationRequestDto(email);
        ProfileDto createdProfile = profileService.register(request);
        UUID profileId = UUID.fromString(createdProfile.getId());

        // Soft delete the profile
        profileService.softDelete(profileId);

        // Try to find soft-deleted profile (should fail)
        assertThatThrownBy(() -> profileService.findById(profileId))
                .isInstanceOf(ProfileException.class)
                .hasMessageContaining("Profile not found by id");

        // Verify outbox event was created
        checkKafkaOutboxEvent(OutboxType.PROFILE_DELETED, profileId.toString());
    }

    @Test
    void testHardDeleteProfile() {
        var request = getProfileRegistrationRequestDto(email);
        request.setPrincipalId(UUID.randomUUID());
        request.setEmail("harddelete@gmail.com");

        ProfileDto newProfile = profileService.register(request);
        UUID newProfileId = UUID.fromString(newProfile.getId());

        // Hard delete the profile
        profileService.hardDelete(newProfileId);

        // Verify profile no longer exists
        assertThatThrownBy(() -> profileService.findById(newProfileId))
                .isInstanceOf(ProfileException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testCleanupOldOutboxRecords() {
        outboxRepository.deleteAll();

        UUID aggregateId1 = UUID.randomUUID();
        UUID aggregateId2 = UUID.randomUUID();
        UUID aggregateId3 = UUID.randomUUID();

        JsonNode payload = objectMapper.valueToTree(Map.of("test", "data"));

        // Create an old record (older than 24 hours)
        OutboxEntity oldRecord = createOutboxRecord(
                aggregateId1.toString(),
                "TestEntity",
                payload,
                OutboxType.PROFILE_CREATED,
                Instant.now().minus(25, ChronoUnit.HOURS) // 25 hours old
        );

        // Create a recent record (less than 24 hours old)
        OutboxEntity recentRecord = createOutboxRecord(
                aggregateId2.toString(),
                "TestEntity",
                payload,
                OutboxType.PROFILE_UPDATED,
                Instant.now().minus(23, ChronoUnit.HOURS) // 23 hours old
        );

        // Create a very recent record
        OutboxEntity veryRecentRecord = createOutboxRecord(
                aggregateId3.toString(),
                "TestEntity",
                payload,
                OutboxType.PROFILE_DELETED,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );

        outboxRepository.saveAll(List.of(oldRecord, recentRecord, veryRecentRecord));

        // Verify all records were saved
        List<OutboxEntity> allRecordsBefore = outboxRepository.findAll();
        assertThat(allRecordsBefore).hasSize(3);

        // Verify individual records exist
        assertThat(outboxRepository.findById(oldRecord.getId())).isPresent();
        assertThat(outboxRepository.findById(recentRecord.getId())).isPresent();
        assertThat(outboxRepository.findById(veryRecentRecord.getId())).isPresent();

        // Act: Manually trigger the cleanup method
        outboxService.cleanupOldOutboxRecords();

        // Assert: Verify cleanup results
        List<OutboxEntity> allRecordsAfter = outboxRepository.findAll();

        // Only the old record (25 hours) should be deleted
        assertThat(allRecordsAfter)
                .hasSize(2)
                .extracting(OutboxEntity::getId)
                .containsExactlyInAnyOrder(recentRecord.getId(), veryRecentRecord.getId());

        // Verify old record is deleted
        assertThat(outboxRepository.findById(oldRecord.getId())).isEmpty();

        // Verify recent records still exist
        assertThat(outboxRepository.findById(recentRecord.getId())).isPresent();
        assertThat(outboxRepository.findById(veryRecentRecord.getId())).isPresent();

        // Verify the correct type of records remain
        assertThat(allRecordsAfter)
                .extracting(OutboxEntity::getType)
                .containsExactlyInAnyOrder(OutboxType.PROFILE_UPDATED, OutboxType.PROFILE_DELETED);
    }

    private OutboxEntity createOutboxRecord(String aggregateId, String aggregateType,
                                            JsonNode payload, OutboxType type,
                                            Instant createdAt) {
        OutboxEntity outboxEntity = new OutboxEntity();
        outboxEntity.setAggregateType(aggregateType);
        outboxEntity.setAggregateId(aggregateId);
        outboxEntity.setType(type);
        outboxEntity.setPayload(payload);
        outboxEntity.setCreated(createdAt);
        return outboxEntity;
    }

    private static ConnectorConfiguration getTestConnectorConfiguration() {
        return ConnectorConfiguration
                .forJdbcContainer(postgresqlContainer)
                .with("plugin.name", "pgoutput")
                .with("topic.prefix", "dbserver");
    }

    private KafkaConsumer<String, String> getConsumer() {
        return new KafkaConsumer<>(
                Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                        ConsumerConfig.GROUP_ID_CONFIG, "tc-" + UUID.randomUUID(),
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        );
        
    }

    private List<ConsumerRecord<String, String>> drain(KafkaConsumer<String, String> consumer,
                                                       int minExpectedRecords) {

        List<ConsumerRecord<String, String>> allRecords = new ArrayList<>();

        Unreliables.retryUntilTrue(10, TimeUnit.SECONDS, () -> {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(50));

            consumerRecords
                    .iterator()
                    .forEachRemaining(allRecords::add);

            return allRecords.size() >= minExpectedRecords;
        });

        return allRecords;
    }

    private ProfileRegistrationRequestDto getProfileRegistrationRequestDto(String email) {
        var request = new ProfileRegistrationRequestDto();
        request.setPrincipalId(UUID.randomUUID());
        request.setAgeMin(18);
        request.setAgeMax(99);
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setEmail(email);
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

    private void checkKafkaOutboxEvent(OutboxType expectedType, String aggregateId) {
        try (KafkaConsumer<String, String> consumer = getConsumer()) {
            consumer.subscribe(List.of(topic));

            List<ConsumerRecord<String, String>> changeEvents = drain(consumer, 1);

            assertThat(changeEvents).hasSize(1);

            ConsumerRecord<String, String> record = changeEvents.get(0);
            assertThat(record.topic()).isEqualTo(topic);

            String value = record.value();
            assertThat(JsonPath.<String>read(value, "$.after.aggregatetype")).isEqualTo("ProfileEntity");
            assertThat(JsonPath.<String>read(value, "$.after.type")).isEqualTo(expectedType.toString());
            assertThat(JsonPath.<String>read(value, "$.after.aggregateid")).isEqualTo(aggregateId);

            consumer.unsubscribe();
        }
    }


    public static MockMultipartFile createMockImage() throws IOException {
        return createMockImage("photo.jpeg");
    }

    public static MockMultipartFile createMockImage(String filename) throws IOException {
        byte[] contentAsByteArray = new ClassPathResource("photo.jpeg").getContentAsByteArray();

        return new MockMultipartFile(
                "file",
                filename,
                MediaType.IMAGE_JPEG_VALUE,
                contentAsByteArray
        );
    }
}
