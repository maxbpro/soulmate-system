package ru.maxb.soulmate.landmark.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StreamUtils;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.common.event.ProfileCreatedDto;
import ru.maxb.soulmate.landmark.common.AbstractKafkaTest;
import ru.maxb.soulmate.landmark.consumer.ProfileCreatedConsumer;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;
import ru.maxb.soulmate.landmark.model.Profile;
import ru.maxb.soulmate.landmark.repository.LandmarkMatchRepository;
import ru.maxb.soulmate.landmark.repository.ProfileRepository;
import ru.maxb.soulmate.landmark.util.DateTimeUtil;
import ru.maxb.soulmate.landmark.util.TestDataLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.StreamSupport;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static ru.maxb.soulmate.landmark.config.KafkaConfiguration.*;


@Slf4j
@SpringBootTest
@Testcontainers
@Tag("integration")
public class LandmarkIntegrationTest extends AbstractKafkaTest {

    private static final int ELASTICSEARCH_PORT = 9200;

    public static final ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer("elasticsearch:7.17.10")
                    .withExposedPorts(ELASTICSEARCH_PORT)
                    .withReuse(true)
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("discovery.type", "single-node")
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                    .withPassword("password") //elastic
                    .withNetwork(network)
                    .withNetworkAliases("elasticsearch");

    @Autowired
    private ProfileCreatedConsumer consumer;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${profile.outbox}")
    private String profileOutboxTopic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private LandmarkMatchRepository landmarkMatchRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private TestDataLoader testDataLoader;

    @BeforeAll
    public static void setUp() {
        elasticsearchContainer.start();
        log.info("Elasticsearch started at: {}", elasticsearchContainer.getHttpHostAddress());
    }

    @AfterAll
    static void tearDownAll() {
        if (elasticsearchContainer.isRunning()) {
            elasticsearchContainer.stop();
        }
    }

    @BeforeEach
    void beforeEach() {
        cleanupTestData();
    }

    @AfterEach
    void tearDown() {
        //cleanupTestData();
    }

    @DynamicPropertySource
    static void elasticSearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
    }


    @Test
    @Order(1)
    @DisplayName("Test infrastructure is running")
    void testContainerIsRunningWithNoExceptions() {
        Assertions.assertThat(kafka.isRunning()).isTrue();
        Assertions.assertThat(elasticsearchContainer.isRunning()).isTrue();
        Assertions.assertThat(elasticsearchContainer.getHttpHostAddress()).isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("Parse Debezium JSON format correctly")
    void parseDebeziumJson() throws Exception {
        // Given
        String json = testDataLoader.loadResourceAsString("debezium/profile_created_example.json");

        // When
        String payload = extractPayloadFromDebezium(json);
        ProfileCreatedDto profileCreatedDto = objectMapper.readValue(payload, ProfileCreatedDto.class);

        // Then
        assertAll(
                () -> Assertions.assertThat(profileCreatedDto).isNotNull(),
                () -> Assertions.assertThat(profileCreatedDto.id()).isNotNull(),
                () -> Assertions.assertThat(profileCreatedDto.landmarks()).isNotEmpty(),
                () -> Assertions.assertThat(profileCreatedDto.gender()).isNotNull(),
                () -> Assertions.assertThat(profileCreatedDto.birthDate()).isNotNull()
        );
    }

    @Test
    @Order(3)
    @DisplayName("Process single profile creation event")
    void processSingleProfileEvent() {
        // Given
        String event = testDataLoader.loadResourceAsString("debezium/profile_created_example.json");
        String expectedProfileId = "58d5b6ca-b775-4175-8f63-db54d2817c53";

        // When
        kafkaTemplate.send(profileOutboxTopic, event);

        // Then
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    long profileCount = profileRepository.count();
                    Assertions.assertThat(profileCount).isEqualTo(1);

                    Optional<Profile> savedProfile = profileRepository.findByProfileId(expectedProfileId);
                    Assertions.assertThat(savedProfile).isPresent();
                    Assertions.assertThat(savedProfile.get().getProfileId()).isEqualTo(expectedProfileId);
                });
    }

    @Test
    @Order(4)
    @DisplayName("Match two profiles based on landmarks")
    void matchTwoProfiles() {
        // Given
        String profileId1 = "58d5b6ca-b775-4175-8f63-db54d2817c53";
        String profileId2 = "28ec38fc-f267-4ec5-a74a-4f41c396fe22";
        String event1 = testDataLoader.getEvent(profileId1);
        String event2 = testDataLoader.getEvent(profileId2);

        // When
        kafkaTemplate.send(profileOutboxTopic, event1);
        await().atMost(Duration.ofSeconds(5)).until(() -> profileRepository.count() == 1);

        kafkaTemplate.send(profileOutboxTopic, event2);

        // Then
        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    // Verify both profiles are saved
                    Assertions.assertThat(profileRepository.count()).isEqualTo(2);

                    // Verify match is created
                    List<LandmarkMatch> matches = testDataLoader.findAllMatches();
                    Assertions.assertThat(matches).hasSize(1);

                    LandmarkMatch match = matches.get(0);
                    assertAll(
                            () -> Assertions.assertThat(match.getProfileId()).isIn(profileId1, profileId2),
                            () -> Assertions.assertThat(match.getSoulmateId()).isIn(profileId1, profileId2),
                            () -> Assertions.assertThat(match.getProfileId()).isNotEqualTo(match.getSoulmateId()),
                            () -> Assertions.assertThat(match.getDistance()).isBetween(0.0, 1.0)
                    );
                });
    }

    @Test
    @Order(5)
    @DisplayName("Do not create duplicate matches")
    void preventDuplicateMatches() {
        // Given
        String profileId1 = "58d5b6ca-b775-4175-8f63-db54d2817c53";
        String profileId2 = "28ec38fc-f267-4ec5-a74a-4f41c396fe22";
        String event1 = testDataLoader.getEvent(profileId1);
        String event2 = testDataLoader.getEvent(profileId2);

        // When - Send same events multiple times
        kafkaTemplate.send(profileOutboxTopic, event1);
        kafkaTemplate.send(profileOutboxTopic, event2);
        kafkaTemplate.send(profileOutboxTopic, event1); // Duplicate
        kafkaTemplate.send(profileOutboxTopic, event2); // Duplicate

        // Then
        await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    Assertions.assertThat(profileRepository.count()).isEqualTo(2);
                    Assertions.assertThat(landmarkMatchRepository.count()).isEqualTo(1);
                });
    }

    @Test
    @Order(6)
    @DisplayName("Handle invalid JSON gracefully")
    void handleInvalidJsonAndVerifyDLT() {
        // Given
        String invalidJson = "{ invalid: json }";
        String originalKey = "test-invalid-json-key";

        // When
        kafkaTemplate.send(profileOutboxTopic, originalKey, invalidJson);

        // Then - Should not crash, might go to DLT
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    // Verify no profiles were created from invalid JSON
                    Assertions.assertThat(profileRepository.count()).isEqualTo(0);
                });

        // Then - Verify DLT routing
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    // Verify no profiles were created
                    Assertions.assertThat(profileRepository.count()).isEqualTo(0);

                    verifyMessageInDLT(DLT_VALIDATION_ERRORS_TOPIC, originalKey);
                });
    }

    private void cleanupTestData() {
        try {
            landmarkMatchRepository.deleteAll();
            profileRepository.deleteAll();
            // Small delay to ensure cleanup is complete
            Thread.sleep(100);
        } catch (Exception e) {
            log.warn("Error during test cleanup: {}", e.getMessage());
        }
    }

    private String extractPayloadFromDebezium(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        return root.path("payload")
                .path("after")
                .path("payload")
                .asText();
    }

    // Helper methods for DLT verification
    private void verifyMessageInDLT(String dltTopic, String expectedKey) {
        try (Consumer<String, String> consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(Collections.singletonList(dltTopic));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(3));

            Assertions.assertThat(records).isNotEmpty();
            Assertions.assertThat(records.count()).isGreaterThan(0);

            // Check if any record has the expected key
            boolean found = false;
            for (ConsumerRecord<String, String> record : records) {
                if (expectedKey.equals(record.key())) {
                    found = true;
                    log.info("Found message in DLT {}: key={}, value={}", dltTopic, record.key(), record.value());
                    break;
                }
            }

            Assertions.assertThat(found)
                    .withFailMessage("Message with key %s not found in DLT topic %s",
                            expectedKey, dltTopic)
                    .isTrue();

        }
    }
}
