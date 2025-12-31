package ru.maxb.soulmate.landmark.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StreamUtils;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.common.event.ProfileCreatedDto;
import ru.maxb.soulmate.landmark.common.AbstractKafkaTest;
import ru.maxb.soulmate.landmark.consumer.ProfileCreatedConsumer;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;
import ru.maxb.soulmate.landmark.repository.LandmarkMatchRepository;
import ru.maxb.soulmate.landmark.repository.ProfileRepository;
import ru.maxb.soulmate.landmark.util.DateTimeUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.awaitility.Awaitility.await;


@Slf4j
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
public class LandmarkIntegrationTest extends AbstractKafkaTest {

    private static final int ELASTICSEARCH_PORT = 9200;

    public static final ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer("elasticsearch:7.17.10")
                    .withExposedPorts(ELASTICSEARCH_PORT)
                    .withReuse(true)
                    .withEnv("xpack.security.enabled", "false")
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

    @BeforeAll
    public static void setUp() {
        elasticsearchContainer.start();
        int port = elasticsearchContainer.getMappedPort(ELASTICSEARCH_PORT);
        log.info("Elasticsearch server started on port {}", port);
    }

    @DynamicPropertySource
    static void elasticSearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
    }


    @Test
    void testContainerIsRunningWithNoExceptions() {
        Assertions.assertThat(kafka.isRunning()).isTrue();
        Assertions.assertThat(elasticsearchContainer.isRunning()).isTrue();
    }

    @Test
    @SneakyThrows
    void startLandmarksProcessing() {
        String debeziumEvent = readTestResourceAsString("debezium/profile_created.json");
        kafkaTemplate.send(profileOutboxTopic, debeziumEvent);

        await().atMost(Duration.ofSeconds(5))
                .until(() -> profileRepository.count() == 1);

        String debeziumEvent2 = readTestResourceAsString("debezium/profile_created2.json");

        kafkaTemplate.send(profileOutboxTopic, debeziumEvent2);

        await().atMost(Duration.ofSeconds(5))
                .until(() -> {

                    Iterator<LandmarkMatch> iterator = landmarkMatchRepository.findAll().iterator();

                    List<LandmarkMatch> matches = StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(iterator, 0),
                            false
                    ).toList();

                    if (matches.isEmpty()) {
                        return false;
                    }
                    LandmarkMatch landmarkMatch  = matches.get(0);

                    return landmarkMatchRepository.count() == 1
                            && landmarkMatch.getSoulmateId().equals("28ec38fc-f267-4ec5-a74a-4f41c396fe22")
                            && landmarkMatch.getProfileId().equals("58d5b6ca-b775-4175-8f63-db54d2817c53");
                });
    }

    @Test
    @SneakyThrows
    void parseDebeziumJson() {
        String json = readTestResourceAsString("debezium/profile_created.json");
        String payload = objectMapper.readTree(json)
                .get("payload")
                .get("after")
                .get("payload")
                .asText();

        ProfileCreatedDto profileCreatedDto = objectMapper.readValue(payload, ProfileCreatedDto.class);

        Assertions.assertThat(profileCreatedDto).isNotNull();
    }

    public String readTestResourceAsString(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
