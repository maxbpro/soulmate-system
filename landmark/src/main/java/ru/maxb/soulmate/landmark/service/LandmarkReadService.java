package ru.maxb.soulmate.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class LandmarkReadService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<LandmarkMatch> searchByTitle(String title) {
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

        SearchHits<LandmarkMatch> searchHits = elasticsearchOperations.search(
                searchQuery,
                LandmarkMatch.class,
                IndexCoordinates.of("landmark_match")
        );

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    public List<LandmarkMatch> findByCoordinate(double lat, double lon, int distance) {
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

        SearchHits<LandmarkMatch> searchHits = elasticsearchOperations.search(
                searchQuery,
                LandmarkMatch.class,
                IndexCoordinates.of("landmark_match")
        );

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

}
