package ru.maxb.soulmate.profile;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.profile.common.AbstractPostgresqlTest;
import ru.maxb.soulmate.profile.model.Gender;
import ru.maxb.soulmate.profile.model.ProfileEntity;
import ru.maxb.soulmate.profile.repository.ProfileRepository;
import ru.maxb.soulmate.profile.service.ObjectStorageService;
import ru.maxb.soulmate.profile.util.DateTimeUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class ProfileRepositoryTest extends AbstractPostgresqlTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ObjectStorageService objectStorageService;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @BeforeEach
    public void init() {
        profileRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    public void createProfile() {
        String photoId = UUID.randomUUID().toString();
        MultipartFile multipartFileFromResource = createMultipartFileFromResource();
        objectStorageService.saveObject(photoId + ".jpg", multipartFileFromResource);

        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setEmail("email");
        profileEntity.setPhoneNumber("+8223232323");
        profileEntity.setAgeMin(18);
        profileEntity.setAgeMax(20);
        profileEntity.setRadius(10);
        profileEntity.setBirthDate(LocalDate.of(1990, 11, 14));
        profileEntity.setInterestedIn(Gender.female);
        profileEntity.setFirstName("firstName");
        profileEntity.setLastName("lastName");
        profileEntity.setActive(true);
        profileEntity.setCreated(dateTimeUtil.now());
        profileEntity.setUpdated(dateTimeUtil.now());
        profileEntity.setPhotos(List.of(photoId));
        profileRepository.save(profileEntity);

        Optional<ProfileEntity> byId = profileRepository.findById(profileEntity.getId());

        assertEquals(profileEntity.getId(), byId.get().getId());
        assertEquals(profileEntity.getEmail(), byId.get().getEmail());
        assertEquals(profileEntity.getPhoneNumber(), byId.get().getPhoneNumber());
        assertEquals(profileEntity.getFirstName(), byId.get().getFirstName());
        assertEquals(profileEntity.getLastName(), byId.get().getLastName());
        assertEquals(profileEntity.getActive(), byId.get().getActive());
        assertEquals(profileEntity.getInterestedIn(), byId.get().getInterestedIn());
        assertEquals(profileEntity.getAgeMax(), byId.get().getAgeMax());
        assertEquals(profileEntity.getAgeMin(), byId.get().getAgeMin());
        assertEquals(1, byId.get().getPhotos().size());

        List<String> photos = objectStorageService.listObjects();

        assertThat(photos).hasSize(1);
        assertThat(photos.get(0)).endsWith(".jpg");
        assertThat(photos.get(0)).startsWith(photoId);
    }



    public MultipartFile createMultipartFileFromResource() throws IOException {
        return new MockMultipartFile(
                "image",         // The name of the parameter in the multipart form (e.g., "file")
                "originalTest.txt",      // The original filename in the client's filesystem
                "image/jpeg",           // The content type of the file (e.g., "application/json", "image/jpeg")
                new ClassPathResource("photo.jpeg").getInputStream() // The content of the file as an InputStream
        );
    }
}
