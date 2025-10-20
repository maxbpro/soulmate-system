package ru.maxb.soulmate.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.profile.common.AbstractPostgresqlTest;
import ru.maxb.soulmate.profile.model.Gender;
import ru.maxb.soulmate.profile.model.ProfileEntity;
import ru.maxb.soulmate.profile.repository.ProfileRepository;
import ru.maxb.soulmate.profile.util.DateTimeUtil;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class ProfileRepositoryTest extends AbstractPostgresqlTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private DateTimeUtil dateTimeUtil;

    @BeforeEach
    public void init() {
        profileRepository.deleteAll();
    }

    @Test
    public void createProfile() {
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
    }

}
