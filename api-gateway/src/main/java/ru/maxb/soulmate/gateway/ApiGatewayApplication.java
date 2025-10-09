package ru.maxb.soulmate.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.maxb.soulmate.gateway.config.AppConfig;
import ru.maxb.soulmate.gateway.config.SecurityConfig;
import ru.maxb.soulmate.gateway.controller.AuthRestControllerV1;
import ru.maxb.soulmate.keycloak.api.KeycloakAuthApiClient;
import ru.maxb.soulmate.user.api.ProfileApiClient;

//@SpringBootApplication(scanBasePackageClasses = {
//        ProfileApiClient.class,
//        KeycloakAuthApiClient.class,
//        SecurityConfig.class,
//        AuthRestControllerV1.class,
//})

@ConfigurationPropertiesScan
@EnableFeignClients(basePackageClasses = {KeycloakAuthApiClient.class, ProfileApiClient.class})
//@SpringBootApplication(scanBasePackageClasses = {
//        KeycloakAuthApiClient.class,
//        ProfileApiClient.class,
//        AppConfig.class
//})
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
