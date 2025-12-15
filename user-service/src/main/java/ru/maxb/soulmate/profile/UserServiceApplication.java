package ru.maxb.soulmate.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.maxb.soulmate.face.api.FaceApiClient;
import ru.maxb.soulmate.landmark.api.LandmarksApiClient;

@SpringBootApplication
@EnableFeignClients(basePackageClasses = {FaceApiClient.class, LandmarksApiClient.class})
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}
