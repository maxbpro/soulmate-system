package ru.maxb.soulmate.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.geo.Point;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.maxb.soulmate.recommendation.common.AbstractElasticSearchTest;
import ru.maxb.soulmate.recommendation.model.Recommendation;
import ru.maxb.soulmate.recommendation.repository.RecommendationRepository;
import ru.maxb.soulmate.recommendation.service.RecommendationReadService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class RecommendationRepositoryTest extends AbstractElasticSearchTest {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private RecommendationReadService recommendationReadService;

    @BeforeEach
    public void init() {
        recommendationRepository.deleteAll();
    }

    @Test
    public void testSave() {
        var recommendation = new Recommendation();
        recommendation.setId("3");
        recommendation.setTitle("title");
        recommendation.setLocation(GeoPoint.fromPoint(new Point(10, 12)));

        Recommendation saved = recommendationRepository.save(recommendation);

        assertEquals(recommendation.getId(), saved.getId());
        assertEquals(recommendation.getTitle(), saved.getTitle());
        assertEquals(recommendation.getLocation().getLat(), 12);
        assertEquals(recommendation.getLocation().getLon(), 10);

        List<Recommendation> recommendationsByTitle = recommendationReadService.searchByTitle("title");
        List<Recommendation> recommendationsByCoordinated = recommendationReadService.findByCoordinate(12, 10, 100);

        assertEquals(recommendationsByTitle.size(), recommendationsByCoordinated.size());
    }
}
