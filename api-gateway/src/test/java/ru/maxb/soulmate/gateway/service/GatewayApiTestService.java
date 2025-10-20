package ru.maxb.soulmate.gateway.service;


import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
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

    public TokenResponse register(GatewayRegistrationRequestDto request) {
        return restTemplate.postForObject(baseUrl() + "/v1/auth/registration", request, TokenResponse.class);
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
