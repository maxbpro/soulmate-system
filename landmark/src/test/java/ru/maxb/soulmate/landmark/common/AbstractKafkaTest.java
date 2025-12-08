package ru.maxb.soulmate.landmark.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@Slf4j
@Testcontainers
@SpringBootTest
public class AbstractKafkaTest {

    private static final int KAFKA_PORT = 9092;

    public static Network network = Network.newNetwork();

    protected static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0")
            .withExposedPorts(KAFKA_PORT)
            .withNetworkAliases("kafka")
            .withNetwork(network)
            .withReuse(true)
            .withListener("kafka:19092");

    @BeforeAll
    public static void setUp() {
        kafka.start();
        int kafkaPort = kafka.getMappedPort(KAFKA_PORT);
        log.info("Kafka started on port {}", kafkaPort);
    }

    @DynamicPropertySource
    static void cassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}
