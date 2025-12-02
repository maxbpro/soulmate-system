package ru.maxb.soulmate.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.maxb.soulmate.keycloak.api.KeycloakAuthApiClient;
import ru.maxb.soulmate.user.api.ProfileApiClient;

@ConfigurationPropertiesScan
@EnableFeignClients(basePackageClasses = {KeycloakAuthApiClient.class, ProfileApiClient.class})
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
