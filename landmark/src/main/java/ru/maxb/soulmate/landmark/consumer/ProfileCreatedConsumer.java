package ru.maxb.soulmate.landmark.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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

import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileCreatedConsumer implements ConsumerSeekAware {

    private final MatchService matchService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${profile.outbox}", groupId = "landmark-group")
    public void profileCreated(ConsumerRecord<String, String> debeziumEventRecord,
                               @Header(KafkaHeaders.OFFSET) Long offset) {
        try {
            String payload = getPayload(debeziumEventRecord);
            ProfileCreatedDto profileCreatedDto = objectMapper.readValue(payload, ProfileCreatedDto.class);
            log.info("received profileId='{}'", profileCreatedDto.id() + ", offset: " + offset);
            matchService.updateProfileRecord(profileCreatedDto);

        } catch (JsonProcessingException e) {
            log.error("error parsing json", e);
        }
    }

    private String getPayload(ConsumerRecord<String, String> debeziumEventRecord) {
        return Optional.ofNullable(debeziumEventRecord)
                .map(ConsumerRecord::value)
                .map(v -> {
                    try {
                        return objectMapper.readTree(v);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(v -> v.get("payload"))
                .map(v -> v.get("after"))
                .map(v -> v.get("payload"))
                .map(JsonNode::asText)
                .orElseThrow(() -> new IllegalArgumentException("Debezium format issue"));
    }
}
