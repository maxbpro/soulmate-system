package ru.maxb.soulmate.gateway.config.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import ru.maxb.soulmate.gateway.exception.ApiException;

public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {
        HttpStatus responseStatus = HttpStatus.valueOf(response.status());

        if (responseStatus.is4xxClientError()) {
            // Handle 4xx client errors
            return new ApiException("Client error: " + response.reason());
        } else if (responseStatus.is5xxServerError()) {
            // Handle 5xx server errors
            return new ApiException("Server error: " + response.reason());
        } else {
            // Fallback to default Feign error handling for other cases
            return new Default().decode("", response);
        }
    }
}
