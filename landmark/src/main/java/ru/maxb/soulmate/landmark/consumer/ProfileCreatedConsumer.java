package ru.maxb.soulmate.landmark.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.maxb.soulmate.common.event.ProfileCreatedDto;
import ru.maxb.soulmate.landmark.exception.ValidationException;
import ru.maxb.soulmate.landmark.service.MatchService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileCreatedConsumer {

    private final MatchService matchService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${profile.outbox}", groupId = "landmark-group")
    public void profileCreated(ConsumerRecord<String, String> debeziumEventRecord,
                               @Header(KafkaHeaders.OFFSET) Long offset,
                               @Header(KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp,
                               Acknowledgment ack) {
        try {
            String payload = extractPayload(debeziumEventRecord);
            ProfileCreatedDto profileCreatedDto = objectMapper.readValue(payload, ProfileCreatedDto.class);

            log.info("Processing profileId='{}', offset={}, timestamp={}",
                    profileCreatedDto.id(), offset, timestamp);

            matchService.updateProfileRecord(profileCreatedDto);

            ack.acknowledge();
            log.debug("Successfully processed: profileId={}, offset={}",
                    profileCreatedDto.id(), offset);

        } catch (JsonProcessingException e) {
            // Will NOT be retried (not retryable)
            throw new ValidationException("Invalid JSON format", e);
        } catch (ValidationException e) {
            // Will NOT be retried (not retryable)
            // But will go to DLT
            throw e;
        } catch (Exception e) {
            // Unknown error - will be retried
            log.error("Unexpected error", e);
            throw new RuntimeException("Processing failed", e);
        }
    }

    private String extractPayload(ConsumerRecord<String, String> debeziumEventRecord) {
        if (debeziumEventRecord == null || debeziumEventRecord.value() == null) {
            throw new ValidationException("Record or value is null");
        }

        try {
            JsonNode root = objectMapper.readTree(debeziumEventRecord.value());

            JsonNode payloadNode = root.path("payload");
            if (payloadNode.isMissingNode()) {
                throw new ValidationException("Missing payload field");
            }

            JsonNode afterNode = payloadNode.path("after");
            if (afterNode.isMissingNode()) {
                throw new ValidationException("Missing after field");
            }

            JsonNode payloadField = afterNode.path("payload");
            if (payloadField.isMissingNode() || !payloadField.isTextual()) {
                throw new ValidationException("Invalid or missing payload field in after");
            }

            return payloadField.asText();

        } catch (JsonProcessingException e) {
            throw new ValidationException("Invalid JSON structure", e);
        }
    }
}
