package ru.maxb.soulmate.landmark.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;

import java.time.Clock;
import java.time.ZoneOffset;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }

    @Bean
    public CommonErrorHandler commonErrorHandler() {
        return new KafkaErrorHandler();
    }
}
