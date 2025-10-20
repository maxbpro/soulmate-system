package ru.maxb.soulmate.recommendation.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.maxb.soulmate.recommendation.model.Recommendation;

public interface RecommendationRepository extends ElasticsearchRepository<Recommendation, String> {

}
