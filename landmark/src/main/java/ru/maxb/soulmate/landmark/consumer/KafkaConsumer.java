package ru.maxb.soulmate.landmark.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.maxb.soulmate.avro.Event;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

//    @KafkaListener(topics = "${test.topic}")
//    public void receive(ConsumerRecord<?, ?> consumerRecord) {
//        log.info("received payload='{}'", consumerRecord.toString());
//        String payload = consumerRecord.toString();
//        //latch.countDown();
//    }

//    @KafkaListener(topics = "${test.topic}")
//    public void receive(Event event) {
//        log.info("received payload='{}'", event.toString());
//        String payload = event.toString();
//        //latch.countDown();
//    }

    @KafkaListener(topics = "${test.topic}", groupId = "my-group-id")
    public void receive(ConsumerRecord<String, Event> consumerRecord) {
        log.info("received payload='{}'", consumerRecord.toString());
        String payload = consumerRecord.toString();
        //latch.countDown();
    }


}
