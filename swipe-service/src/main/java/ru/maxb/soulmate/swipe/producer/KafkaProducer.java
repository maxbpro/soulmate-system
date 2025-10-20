package ru.maxb.soulmate.swipe.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.maxb.soulmate.avro.Event;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Event> kafkaTemplate;

    public void send(String topic, Event event) {
        kafkaTemplate.send(topic, event);
    }
}
