package ru.maxb.soulmate.landmark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.maxb.soulmate.avro.Event;
import ru.maxb.soulmate.landmark.common.AbstractKafkaTest;
import ru.maxb.soulmate.landmark.consumer.KafkaConsumer;
import ru.maxb.soulmate.landmark.consumer.KafkaProducer;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KafkaConsumerTest extends AbstractKafkaTest {

    @Autowired
    private KafkaProducer producer;

    @Autowired
    private KafkaConsumer consumer;

    @Value("${test.topic}")
    private String topic;

    @Test
    public void givenKafkaBroker_whenSendingWithSimpleProducer_thenMessageReceived() {

        Event event = Event.newBuilder()
                .setUid(UUID.randomUUID().toString())
                .setSubject("event")
                .setDescription("description")
                .build();


        producer.send(topic, event);


    }
}
