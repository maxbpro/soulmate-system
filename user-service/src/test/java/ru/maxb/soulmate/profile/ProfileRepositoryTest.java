package ru.maxb.soulmate.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.profile.model.ProfileEntity;
import ru.maxb.soulmate.profile.repository.ProfileRepository;
import ru.maxb.soulmate.profile.util.DateTimeUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class ProfileRepositoryTest extends AbstractPostgesqlTest {

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
        profileEntity.setFirstName("firstName");
        profileEntity.setLastName("lastName");
        profileEntity.setActive(true);
        profileEntity.setCreated(dateTimeUtil.now());
        profileEntity.setUpdated(dateTimeUtil.now());
        profileRepository.save(profileEntity);

        Optional<ProfileEntity> byId = profileRepository.findById(profileEntity.getId());

        assertEquals(profileEntity.getId(), byId.get().getId());
        assertEquals(profileEntity.getEmail(), byId.get().getEmail());
        assertEquals(profileEntity.getFirstName(), byId.get().getFirstName());
        assertEquals(profileEntity.getLastName(), byId.get().getLastName());
        assertEquals(profileEntity.getActive(), byId.get().getActive());
    }

    @Test
    public void shouldFindProfile() {
        Iterable<ProfileEntity> profiles = profileRepository.findAll();

        if (profiles.iterator().hasNext()) {

        }
    }
}
