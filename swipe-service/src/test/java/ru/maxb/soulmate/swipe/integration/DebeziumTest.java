package ru.maxb.soulmate.swipe.integration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.jayway.jsonpath.JsonPath;
import io.debezium.testing.testcontainers.ConnectorConfiguration;
import io.debezium.testing.testcontainers.DebeziumContainer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
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

    @MockitoBean
    private CqlSession cassandraSession;

    @BeforeAll
    public static void setUp() {
        Startables.deepStart(Stream.of(kafka, postgresqlContainer, debeziumContainer))
                .join();

        log.info("Kafka started on port {}", kafka.getMappedPort(KAFKA_PORT));
        log.info("Postgresql started on port {}", postgresqlContainer.getMappedPort(POSTGRES_PORT));
        log.info("Debezium started on port {}", debeziumContainer.getMappedPort(DEBEZIUM_PORT));
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

    @Test
    public void canRegisterPostgreSqlConnector() throws Exception {
        try (Connection connection = getConnection(postgresqlContainer);
             Statement statement = connection.createStatement();
             KafkaConsumer<String, String> consumer = getConsumer(kafka)) {

            statement.execute("create schema todo");
            statement.execute("create table todo.Todo (id int8 not null, " +
                    "title varchar(255), primary key (id))");
            statement.execute("alter table todo.Todo replica identity full");
            statement.execute("insert into todo.Todo values (1, " +
                    "'Learn CDC')");
            statement.execute("insert into todo.Todo values (2, " +
                    "'Learn Debezium')");

            ConnectorConfiguration connector = ConnectorConfiguration
                    .forJdbcContainer(postgresqlContainer)
                    .with("plugin.name", "pgoutput") //todo
                    .with("topic.prefix", "dbserver1");

            debeziumContainer.registerConnector("my-connector", connector);

            consumer.subscribe(Arrays.asList("dbserver1.todo.todo"));

            List<ConsumerRecord<String, String>> changeEvents =
                    drain(consumer, 2);

            assertThat(JsonPath.<Integer>read(changeEvents.get(0).key(),
                    "$.id")).isEqualTo(1);

            consumer.unsubscribe();
        }
    }

    private KafkaConsumer<String, String> getConsumer(
            KafkaContainer kafkaContainer) {

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
            consumer.poll(Duration.ofMillis(50))
                    .iterator()
                    .forEachRemaining(allRecords::add);

            return allRecords.size() == expectedRecordCount;
        });

        return allRecords;
    }

    private Connection getConnection(PostgreSQLContainer<?> postgresContainer) throws SQLException {
        return DriverManager.getConnection(postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword());
    }
}

