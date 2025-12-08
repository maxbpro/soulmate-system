package ru.maxb.soulmate.landmark.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.maxb.soulmate.common.event.ProfileCreatedDto;
import ru.maxb.soulmate.landmark.service.MatchService;


@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileCreatedConsumer implements ConsumerSeekAware {

    private final MatchService matchService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${profile.outbox}", groupId = "landmark-group")
    public void newProfileCreated3(ConsumerRecord<String, String> debeziumEventRecord,
                                   @Header(KafkaHeaders.OFFSET) Long offset) {
        try {
            String debeziumEventJson = debeziumEventRecord.value();
            String payload = objectMapper.readTree(debeziumEventJson).get("after").get("payload").asText();

            ProfileCreatedDto profileCreatedDto = objectMapper.readValue(payload, ProfileCreatedDto.class);
            log.info("received profileId='{}'", profileCreatedDto.id() + ", offset: " + offset);

            matchService.startMatchingProcess(profileCreatedDto);

        } catch (JsonProcessingException e) {
            log.error("error parsing json", e);
        }
    }

}
