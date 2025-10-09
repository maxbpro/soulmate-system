package ru.maxb.soulmate.gateway;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.TokenResponse;
import ru.maxb.soulmate.gateway.dto.UserInfoResponse;
import ru.maxb.soulmate.gateway.service.GatewayApiTestService;
import ru.maxb.soulmate.gateway.service.KeycloakApiTestService;
import ru.maxb.soulmate.gateway.util.Setting;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.maxb.soulmate.gateway.containers.KeycloakTestContainer.KEYCLOAK_PORT;

@Testcontainers
public class AuthRestControllerV1Test extends AbstractKeycloakTest {

    @Autowired
    private GatewayApiTestService gatewayApiTestService;

    @Autowired
    private KeycloakApiTestService keycloakApiTestService;

    @Test
    public void test() {
        //when
        var request = new GatewayRegistrationRequestDto();

        var response = gatewayApiTestService.register(request);
        var meResponse = gatewayApiTestService.getMe(response.getAccessToken());

        var personId = keycloakApiTestService
                .getUserRepresentation(request.getEmail())
                .getId();

        // then
        assertTrue(StringUtils.isNoneBlank(personId));
        assertNotNull(response, "Response must not be null");
        assertNotNull(response.getAccessToken(), "Access token must not be null");
        assertEquals("Bearer", response.getTokenType(), "Token type must be Bearer");
        assertEquals(request.getEmail(), meResponse.getEmail());

        String kcBase = "http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(KEYCLOAK_PORT);

        Map<String, Object> p = new HashMap<>();
        //p.put("application.person.url", wireMockBase);
        p.put("application.keycloak.serverUrl", kcBase);
        p.put("application.keycloak.realm", Setting.REALM);

        p.put("spring.security.oauth2.resourceserver.jwt.issuer-uri", kcBase + "/realms/" + Setting.REALM);


        UserInfoResponse me = gatewayApiTestService.getMe("");

        String email = me.getEmail();
    }
}
