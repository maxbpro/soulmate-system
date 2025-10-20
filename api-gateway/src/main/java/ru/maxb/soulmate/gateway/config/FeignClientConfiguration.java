package ru.maxb.soulmate.gateway.config;

import feign.codec.ErrorDecoder;
import org.openapitools.configuration.ClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.maxb.soulmate.gateway.config.feign.FeignErrorDecoder;

@Configuration
public class FeignClientConfiguration extends ClientConfiguration {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
