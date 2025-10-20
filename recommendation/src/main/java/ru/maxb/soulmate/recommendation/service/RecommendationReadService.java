package ru.maxb.soulmate.recommendation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.recommendation.model.Recommendation;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RecommendationReadService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<Recommendation> searchByTitle(String title) {
        String queryString = """
                {
                    "bool": {
                      "must": [
                        {
                          "match": {
                            "title": "%s"
                          }
                        }
                      ]
                    }
                }
                """.formatted(title);

        Query searchQuery = new StringQuery(queryString);

        SearchHits<Recommendation> searchHits = elasticsearchOperations.search(
                searchQuery,
                Recommendation.class,
                IndexCoordinates.of("recommendation")
        );

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    public List<Recommendation> findByCoordinate(double lat, double lon, int distance) {
        String queryString = """
                {
                    "geo_distance":{
                      "location":{
                          "lat":%.3f,
                          "lon":%.3f
                      },
                      "distance":"%d"
                    }
                }
                """.formatted(lat, lon, distance);
        Query searchQuery = new StringQuery(queryString);

        SearchHits<Recommendation> searchHits = elasticsearchOperations.search(
                searchQuery,
                Recommendation.class,
                IndexCoordinates.of("recommendation")
        );

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

}
