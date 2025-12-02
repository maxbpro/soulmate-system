package ru.maxb.soulmate.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.maxb.soulmate.face.api.FaceApiClient;

@SpringBootApplication
@EnableFeignClients(basePackageClasses = {FaceApiClient.class})
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}
