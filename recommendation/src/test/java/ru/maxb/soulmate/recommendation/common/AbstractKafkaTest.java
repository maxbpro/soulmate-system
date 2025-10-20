package ru.maxb.soulmate.recommendation.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import ru.maxb.soulmate.recommendation.repository.RecommendationRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
@SpringBootTest
public class AbstractKafkaTest {

    private static final int KAFKA_PORT = 9092;
    private static final int SCHEMA_REGISTRY_PORT = 8091;

    public static Network network = Network.newNetwork();

    public static GenericContainer schemaRegistryContainer = new GenericContainer<>("confluentinc/cp-schema-registry:7.5.0")
            .withExposedPorts(SCHEMA_REGISTRY_PORT)
            .withNetworkAliases("schema-registry")
            .withNetwork(network)
            .withReuse(true)
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:19092")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:" + SCHEMA_REGISTRY_PORT)
            .waitingFor(Wait.forHttp("/subjects").forStatusCode(200));


    public static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0")
            .withExposedPorts(KAFKA_PORT)
            .withNetworkAliases("kafka")
            .withNetwork(network)
            .withReuse(true)
            .withListener("kafka:19092");


    @MockitoBean
    private RecommendationRepository recommendationRepository;

    @BeforeAll
    public static void setUp() {
        kafka.start();
        int kafkaPort = kafka.getMappedPort(KAFKA_PORT);
        log.info("Kafka started on port {}", kafkaPort);

        schemaRegistryContainer.dependsOn(kafka);
        schemaRegistryContainer.start();
        int schemaRegistryPort = schemaRegistryContainer.getMappedPort(SCHEMA_REGISTRY_PORT);
        log.info("SchemaRegistry started on port {}", schemaRegistryPort);
    }

    @DynamicPropertySource
    static void cassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        int schemaRegistryPort = schemaRegistryContainer.getMappedPort(SCHEMA_REGISTRY_PORT);

        registry.add("kafka.schema-registry-url",
                () -> "http://localhost:" + schemaRegistryPort);

        registry.add("spring.kafka.properties.schema.registry.url",
                () -> "http://localhost:" + schemaRegistryPort);

    }

    @Test
    void testContainerIsRunningWithNoExceptions() {
        assertThat(schemaRegistryContainer.isRunning()).isTrue();
        assertThat(kafka.isRunning()).isTrue();
    }
}
