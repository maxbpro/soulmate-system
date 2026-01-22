package ru.maxb.soulmate.gateway;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.TokenRefreshRequest;
import ru.maxb.soulmate.gateway.dto.TokenResponse;
import ru.maxb.soulmate.gateway.dto.UserLoginRequest;
import ru.maxb.soulmate.gateway.service.GatewayApiTestService;
import ru.maxb.soulmate.gateway.service.KeycloakApiTestService;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class ApiGatewayIntegrationTest extends AbstractKeycloakTest {

    @Autowired
    private GatewayApiTestService gatewayApiTestService;

    @Autowired
    private KeycloakApiTestService keycloakApiTestService;

    @SneakyThrows
    @Test
    public void shouldCreateNewUserAndReturnAccessToken() {
        //when
        var registerRequest = getGatewayRegistrationRequestDto();

        var response = gatewayApiTestService.register(registerRequest);
        var meResponse = gatewayApiTestService.getMe(response.getAccessToken());

        var personId = keycloakApiTestService
                .getUserRepresentation(registerRequest.getEmail())
                .getId();

        // then
        assertTrue(StringUtils.isNoneBlank(personId));
        assertNotNull(response, "Response must not be null");
        assertNotNull(response.getAccessToken(), "Access token must not be null");
        assertEquals("Bearer", response.getTokenType(), "Token type must be Bearer");
        assertEquals(registerRequest.getEmail(), meResponse.getEmail());
    }

    @SneakyThrows
    @Test
    void shouldLoginAndReturnAccessToken() {
        // given: регистрируем пользователя
        var registerRequest = getGatewayRegistrationRequestDto();
        gatewayApiTestService.register(registerRequest);

        // when: логинимся тем же email/password
        var loginRequest = getUserLoginRequest();
        var response = gatewayApiTestService.login(loginRequest);
        var meResponse = gatewayApiTestService.getMe(response.getAccessToken());

        // then
        assertNotNull(response, "Response must not be null");
        assertNotNull(response.getAccessToken(), "Access token must not be null");
        assertEquals("Bearer", response.getTokenType(), "Token type must be Bearer");
        assertEquals(registerRequest.getEmail(), meResponse.getEmail());
    }

    @SneakyThrows
    @Test
    void shouldReturnUserInfo() {
        // given
        var registerRequest = getGatewayRegistrationRequestDto();
        var registrationResponse = gatewayApiTestService.register(registerRequest);

        // when
        var meResponse = gatewayApiTestService.getMe(registrationResponse.getAccessToken());

        // then
        assertNotNull(meResponse.getEmail(), "email in /me must be present");
        assertEquals(registerRequest.getEmail(), meResponse.getEmail(), "emails must match");
    }

    @SneakyThrows
    @Test
    void shouldRefreshToken() {
        // given: регистрируем пользователя
        var registerRequest = getGatewayRegistrationRequestDto();
        TokenResponse tokenResponse = gatewayApiTestService.register(registerRequest);
        var tokenRefreshRequest = getTokenRefreshRequest(tokenResponse.getRefreshToken());

        // when
        TokenResponse response = gatewayApiTestService.refreshToken(tokenRefreshRequest);
        var meResponse = gatewayApiTestService.getMe(response.getAccessToken());

        // then
        assertNotNull(response, "Response must not be null");
        assertNotNull(response.getAccessToken(), "Access token must not be null");
        assertEquals("Bearer", response.getTokenType(), "Token type must be Bearer");
        assertEquals(registerRequest.getEmail(), meResponse.getEmail());
    }

    private GatewayRegistrationRequestDto getGatewayRegistrationRequestDto() {
        var request = new GatewayRegistrationRequestDto();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setEmail("john.smith@gmail.com");
        request.setPassword("password");
        request.setConfirmPassword("password");
        request.setPhoneNumber("1234567890");
        request.setPhoto(getBase64Image());
        return request;
    }

    private UserLoginRequest getUserLoginRequest() {
        var request = new UserLoginRequest();
        request.setEmail("john.smith@gmail.com");
        request.setPassword("password");
        return request;
    }

    private TokenRefreshRequest getTokenRefreshRequest(String refreshToken) {
        var request = new TokenRefreshRequest();
        request.setRefreshToken(refreshToken);
        return request;
    }

    @SneakyThrows
    public String getBase64Image() {
        return Base64.getEncoder().encodeToString(
                new ClassPathResource("photo.jpeg").getContentAsByteArray());
    }
}
