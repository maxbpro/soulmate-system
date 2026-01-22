package ru.maxb.soulmate.landmark.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.protocol.types.SchemaException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.*;
import org.springframework.util.backoff.FixedBackOff;
import ru.maxb.soulmate.landmark.exception.ValidationException;

import java.net.ConnectException;

@Slf4j
@Configuration
public class KafkaConfiguration {

    public static final String DLT_VALIDATION_ERRORS_TOPIC = "users-dlt-validation-errors";
    public static final String DLT_GENERAL_ERRORS_TOPIC = "users-dlt-general-errors";

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaOperations<String, Object> kafkaOperations) {
        return new DeadLetterPublishingRecoverer(kafkaOperations,
                (record, ex) -> {

                    // Unwrap ListenerExecutionFailedException to get the actual cause
                    Throwable actualCause = getRootCause(ex);

                    log.error("Routing to DLT for exception: {} -> {}",
                            ex.getClass().getName(), actualCause.getClass().getName());

                    if (actualCause instanceof ValidationException || actualCause instanceof JsonParseException) {
                        return new TopicPartition(DLT_VALIDATION_ERRORS_TOPIC, record.partition());
                    } else {
                        return new TopicPartition(DLT_GENERAL_ERRORS_TOPIC, record.partition());
                    }
                }) {
        };
    }

    @Bean
    public CommonErrorHandler commonErrorHandler(DeadLetterPublishingRecoverer deadLetterRecoverer) {
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2); // 2 retries = 3 total attempts

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterRecoverer, fixedBackOff);
        errorHandler.setRetryListeners(new RetryListener() {
            @Override
            public void failedDelivery(ConsumerRecord<?, ?> record, Exception ex,
                                       int deliveryAttempt) {
                log.warn("Delivery failed for record [{}] at attempt {}: {}",
                        record.key(), deliveryAttempt, ex.getMessage());
            }

            @Override
            public void recovered(ConsumerRecord<?, ?> record, Exception ex) {
                log.info("Record [{}] recovered after retries, sent to DLT", record.key());
            }
        });

        errorHandler.addNotRetryableExceptions(
                ValidationException.class,      // Data validation errors
                IllegalArgumentException.class, // Bad input
                JsonParseException.class,       // Malformed JSON
                SchemaException.class          // Schema violations
        );

        errorHandler.addRetryableExceptions(
                //ElasticsearchException.class,   // ES temporary issues
                ConnectException.class,         // Network issues
                TimeoutException.class,         // Timeouts
                TransientDataAccessException.class // DB transient errors
        );

        return errorHandler;
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}
