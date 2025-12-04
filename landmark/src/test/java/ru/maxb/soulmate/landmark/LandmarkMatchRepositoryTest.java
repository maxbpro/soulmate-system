package ru.maxb.soulmate.landmark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.geo.Point;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.landmark.common.AbstractElasticSearchTest;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;
import ru.maxb.soulmate.landmark.repository.LandmarkMatchRepository;
import ru.maxb.soulmate.landmark.service.LandmarkReadService;

import java.time.LocalDate;
import java.util.List;

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
    public void testSave() {
        var landmarkMatch = new LandmarkMatch();
        landmarkMatch.setId("3");
        landmarkMatch.setDateOfBirth(LocalDate.of(1990, 11, 11));
        landmarkMatch.setLocation(GeoPoint.fromPoint(new Point(10, 12)));
        landmarkMatch.setGender("female");
        LandmarkMatch saved = landmarkMatchRepository.save(landmarkMatch);

        assertEquals(landmarkMatch.getId(), saved.getId());
        assertEquals(landmarkMatch.getDateOfBirth(), saved.getDateOfBirth());
        assertEquals(landmarkMatch.getLocation().getLat(), 12);
        assertEquals(landmarkMatch.getLocation().getLon(), 10);

        List<LandmarkMatch> landmarkMatchesByCoordinated = landmarkReadService.findByCoordinate(12, 10, 100);

        //List<LandmarkMatch> byTitle = landmarkReadService.searchByTitle("title");

        //assertEquals(recommendationsByTitle.size(), landmarkMatchesByCoordinated.size());
    }
}
