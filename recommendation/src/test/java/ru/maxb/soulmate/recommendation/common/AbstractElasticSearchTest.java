package ru.maxb.soulmate.recommendation.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class AbstractElasticSearchTest {

    private static final int ELASTICSEARCH_PORT = 9200;

    public static final ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer("elasticsearch:7.17.10")
                    .withExposedPorts(ELASTICSEARCH_PORT)
                    .withReuse(true)
                    .withEnv("xpack.security.enabled", "false")
                    .withPassword("password") //elastic
                    .withNetworkAliases("elasticsearch");

    @BeforeAll
    public static void setUp() {
        elasticsearchContainer.start();
        int port = elasticsearchContainer.getMappedPort(ELASTICSEARCH_PORT);
        log.info("Elasticsearch server started on port {}", port);
    }

    @DynamicPropertySource
    static void cassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
    }

    @Test
    void givenElasticSearchContainer_whenSpringContextIsBootstrapped_thenContainerIsRunningWithNoExceptions() {
        assertThat(elasticsearchContainer.isRunning()).isTrue();
    }
}
