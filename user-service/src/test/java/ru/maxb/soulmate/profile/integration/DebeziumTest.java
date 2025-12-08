package ru.maxb.soulmate.profile.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.testing.testcontainers.ConnectorConfiguration;
import io.debezium.testing.testcontainers.DebeziumContainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.PersistenceContext;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import ru.maxb.soulmate.profile.model.Gender;
import ru.maxb.soulmate.profile.model.OutboxEntity;
import ru.maxb.soulmate.profile.model.ProfileEntity;
import ru.maxb.soulmate.profile.repository.OutboxRepository;
import ru.maxb.soulmate.profile.repository.ProfileRepository;
import ru.maxb.soulmate.profile.util.DateTimeUtil;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
@SpringBootTest
public class DebeziumTest {

    private static final int KAFKA_PORT = 9092;
    private static final int POSTGRES_PORT = 5432;
    private static final int DEBEZIUM_PORT = 8083;
    private static final String POSTGRES_DATABASE_NAME = "test";

    public static Network network = Network.newNetwork();

    public static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0")
            .withExposedPorts(KAFKA_PORT)
            .withNetworkAliases("kafka")
            .withNetwork(network)
            .withReuse(true)
            .withListener("kafka:19092");

    private static PostgreSQLContainer postgresqlContainer =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withExposedPorts(POSTGRES_PORT)
                    .withDatabaseName(POSTGRES_DATABASE_NAME)
                    .withUsername("customer")
                    .withPassword("password")
                    .withReuse(true)
                    .withNetwork(network)
                    .withNetworkAliases("postgres")
                    .withEnv("POSTGRES_INITDB_ARGS", "-c wal_level=logical");

    public static DebeziumContainer debeziumContainer =
            new DebeziumContainer("quay.io/debezium/connect:3.3.1.Final")
                    .withExposedPorts(DEBEZIUM_PORT)
                    .withNetwork(network)
                    .withKafka(network, "kafka:19092")
                    .dependsOn(kafka)
                    .withReuse(true);

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;


    @BeforeAll
    public static void setUp() {
        Startables.deepStart(Stream.of(kafka, postgresqlContainer, debeziumContainer))
                .join();

        log.info("Postgresql started on port {}", postgresqlContainer.getMappedPort(POSTGRES_PORT));
        log.info("Debezium started on port {}", debeziumContainer.getMappedPort(DEBEZIUM_PORT));
        log.info("Kafka started on port {}", kafka.getMappedPort(KAFKA_PORT));
    }

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", postgresqlContainer::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", postgresqlContainer::getPassword);
        dynamicPropertyRegistry.add("spring.datasource.driver-class-name", postgresqlContainer::getDriverClassName);
    }


    @Test
    void testContainerAreRunningWithNoExceptions() {
        assertThat(kafka.isRunning()).isTrue();
        assertThat(postgresqlContainer.isRunning()).isTrue();
        assertThat(debeziumContainer.isRunning()).isTrue();
    }

//    @Test
//    public void canRegisterPostgreSqlConnector() throws Exception {
//        ConnectorConfiguration connector = ConnectorConfiguration
//                .forJdbcContainer(postgresqlContainer)
//                .with("topic.prefix", "dbserver1");
//
//        debeziumContainer.registerConnector("my-connector", connector);
//        KafkaConsumer<String, String> consumer = getConsumer(kafka);
//        List<ConsumerRecord<String, String>> changeEvents = drain(consumer, 2);
//
//        consumer.unsubscribe();
//    }

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @Test
    @SneakyThrows
    public void canRegisterPostgreSqlConnector() {
        profileRepository.deleteAll();
        outboxRepository.deleteAll();

        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setEmail("email");
        profileEntity.setPhoneNumber("+8223232323");
        profileEntity.setAgeMin(18);
        profileEntity.setAgeMax(20);
        profileEntity.setRadius(10);
        profileEntity.setBirthDate(LocalDate.of(1990, 11, 14));
        profileEntity.setInterestedIn(Gender.FEMALE);
        profileEntity.setFirstName("firstName");
        profileEntity.setLastName("lastName");
        profileEntity.setActive(true);
        profileEntity.setLandmarks("test value");
        profileEntity.setGender(Gender.MALE);
        profileEntity.setCreated(dateTimeUtil.now());
        profileEntity.setUpdated(dateTimeUtil.now());
        profileRepository.save(profileEntity);

        OutboxEntity outboxEntity = new OutboxEntity();
        outboxEntity.setAggregateType(ProfileEntity.class.getSimpleName());
        outboxEntity.setAggregateId(profileEntity.getId().toString());
        outboxEntity.setType("Profile created");
        outboxEntity.setPayload(new ObjectMapper().readTree(" \"{\"postCode\": \"E4 8ST, \"city\":\"London}\""));

        try (KafkaConsumer<String, String> consumer = getConsumer(kafka)) {

            debeziumContainer.registerConnector("profile-connector", getTestConnectorConfiguration());
            consumer.subscribe(Arrays.asList("dbserver.profile.outbox"));

            //

            transactionTemplate.executeWithoutResult(status -> {

                outboxRepository.save(outboxEntity);

                entityManager.setFlushMode(FlushModeType.COMMIT);
                entityManager.flush();
            });


            //


            List<ConsumerRecord<String, String>> changeEvents = drain(consumer, 1);

//            assertThat(JsonPath.<String>read(changeEvents.get(0).key(),
//                    "$.id")).isEqualTo(1);

            consumer.unsubscribe();
        }
    }

    private ConnectorConfiguration getTestConnectorConfiguration() {
        return ConnectorConfiguration
                .forJdbcContainer(postgresqlContainer)
                .with("plugin.name", "pgoutput") //todo
                .with("topic.prefix", "dbserver");

    }

    private KafkaConsumer<String, String> getConsumer(KafkaContainer kafkaContainer) {
        return new KafkaConsumer<>(
                Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                        ConsumerConfig.GROUP_ID_CONFIG, "tc-" + UUID.randomUUID(),
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
                ));
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

}

