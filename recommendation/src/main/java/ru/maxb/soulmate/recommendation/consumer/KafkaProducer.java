package ru.maxb.soulmate.recommendation.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.maxb.soulmate.avro.Event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Event> kafkaTemplate;

    public void send(String topic, Event event) {
        CompletableFuture<SendResult<String, Event>> send = kafkaTemplate.send(topic, event);

        try {
            SendResult<String, Event> stringEventSendResult = send.get();
            RecordMetadata recordMetadata = stringEventSendResult.getRecordMetadata();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
