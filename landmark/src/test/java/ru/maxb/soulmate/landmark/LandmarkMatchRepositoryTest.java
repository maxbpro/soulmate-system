package ru.maxb.soulmate.landmark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.geo.Point;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.landmark.common.AbstractElasticSearchTest;
import ru.maxb.soulmate.landmark.model.Gender;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;
import ru.maxb.soulmate.landmark.repository.LandmarkMatchRepository;
import ru.maxb.soulmate.landmark.service.LandmarkReadService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class LandmarkMatchRepositoryTest extends AbstractElasticSearchTest {

    @Autowired
    private LandmarkMatchRepository landmarkMatchRepository;

    @Autowired
    private LandmarkReadService landmarkReadService;

    @BeforeEach
    public void init() {
        landmarkMatchRepository.deleteAll();
    }

    @Test
    public void testSearch() {
        UUID profileId = UUID.randomUUID();

        UUID soulmateId1 = UUID.randomUUID();
        UUID soulmateId2 = UUID.randomUUID();
        UUID soulmateIdNotMatched = UUID.randomUUID();
        UUID soulmateIdNotMatchedByLocation = UUID.randomUUID();
        UUID soulmateIdNotMatchedByAge = UUID.randomUUID();

        LandmarkMatch profile = getLandmarkMatch(Gender.MALE,
                LocalDate.of(1990, 11, 14), 10, 12,
                profileId, UUID.randomUUID());

        LandmarkMatch soulmateMatch1 = getLandmarkMatch(Gender.FEMALE,
                LocalDate.of(1990, 11, 12), 10, 12,
                soulmateId1, profileId);

        LandmarkMatch soulmateMatch2 = getLandmarkMatch(Gender.FEMALE,
                LocalDate.of(1998, 11, 12), 10, 12,
                soulmateId2, profileId);

        LandmarkMatch soulmateNotMatched = getLandmarkMatch(Gender.FEMALE,
                LocalDate.of(1990, 11, 12), 10, 12,
                soulmateIdNotMatched, UUID.randomUUID());

        LandmarkMatch soulmateNotMatchedByLocation = getLandmarkMatch(Gender.FEMALE,
                LocalDate.of(1990, 11, 12), 43, 13,
                soulmateIdNotMatchedByLocation, profileId);

        LandmarkMatch soulmateNotMatchedByAge = getLandmarkMatch(Gender.FEMALE,
                LocalDate.of(1976, 11, 12), 10, 12,
                soulmateIdNotMatchedByAge, profileId);

        landmarkMatchRepository.save(profile);
        landmarkMatchRepository.save(soulmateMatch1);
        landmarkMatchRepository.save(soulmateMatch2);
        landmarkMatchRepository.save(soulmateNotMatched);
        landmarkMatchRepository.save(soulmateNotMatchedByLocation);
        landmarkMatchRepository.save(soulmateNotMatchedByAge);


        List<LandmarkMatch> byParams = landmarkReadService.findByParams(12, 10, 1000,
                18, 32, Gender.FEMALE, profileId.toString());

        assertEquals(byParams.size(), 1);
    }

    private LandmarkMatch getLandmarkMatch(Gender gender, LocalDate birthDate,
                                           int lat, int lon,
                                           UUID profileId, UUID soulmateId) {
        var landmarkMatch = new LandmarkMatch();
        landmarkMatch.setId(UUID.randomUUID().toString());

        landmarkMatch.setProfileId(profileId.toString());
        landmarkMatch.setSoulmateId(soulmateId.toString());

        landmarkMatch.setDateOfBirth(birthDate);
        landmarkMatch.setLocation(new GeoPoint(lat, lon));
        landmarkMatch.setGender(gender);

        return landmarkMatch;
    }
}
