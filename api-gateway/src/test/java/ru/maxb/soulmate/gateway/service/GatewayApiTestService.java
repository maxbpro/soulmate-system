package ru.maxb.soulmate.gateway.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.gateway.dto.*;

@Component
public class GatewayApiTestService {

    private final RestTemplate restTemplate;
    private final Environment env;

    public GatewayApiTestService(RestTemplate restTemplate, Environment env) {
        this.restTemplate = restTemplate;
        this.env = env;
    }

    private String baseUrl() {
        Integer port = env.getProperty("local.server.port", Integer.class);
        if (port == null || port == 0) {
            port = env.getProperty("server.port", Integer.class, 8080);
        }
        return "http://localhost:" + port;
    }

    @SneakyThrows
    public TokenResponse register(MultipartFile imageFile,
                                  GatewayRegistrationRequestDto request) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(request);

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> jsonPart = new HttpEntity<>(json, jsonHeaders);
        body.add("profileRequestDto", jsonPart);

        //ByteArrayResource byteArrayResource = new ByteArrayResource(imageFile.getBytes());
        body.add("image_file", imageFile.getBytes());

//        HttpMessageConverter<Object> jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
//                new MappingJackson2HttpMessageConverter();
//
//        HttpMessageConverter<Resource> resourceHttpMessageConverter = new ResourceHttpMessageConverter();
//                new ResourceHttpMessageConverter();
//
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, requestHeaders);

        //restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        //restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        ResponseEntity<TokenResponse> tokenResponseResponseEntity = restTemplate.postForEntity(
                baseUrl() + "/v1/auth/registration", requestEntity, TokenResponse.class);

        return tokenResponseResponseEntity.getBody();
    }

    public TokenResponse login(UserLoginRequest request) {
        return restTemplate.postForObject(baseUrl() + "/v1/auth/login", request, TokenResponse.class);
    }

    public UserInfoResponse getMe(String accessToken) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        var resp = restTemplate.exchange(baseUrl() + "/v1/auth/me", HttpMethod.GET, new HttpEntity<>(headers),
                UserInfoResponse.class);
        return resp.getBody();
    }

    public TokenResponse refreshToken(TokenRefreshRequest request) {
        return restTemplate.postForObject(baseUrl() + "/v1/auth/refresh-token", request, TokenResponse.class);
    }
}
