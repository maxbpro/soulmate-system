package ru.maxb.soulmate.landmark.repository;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;

import java.util.Optional;

public interface LandmarkMatchRepository extends ElasticsearchRepository<LandmarkMatch, String> {

    @Query("""
        {
          "bool": {
            "should": [
              {
                "bool": {
                  "must": [
                    { "term": { "profileId.keyword": "?0" } },
                    { "term": { "soulmateId.keyword": "?1" } }
                  ]
                }
              },
              {
                "bool": {
                  "must": [
                    { "term": { "profileId.keyword": "?1" } },
                    { "term": { "soulmateId.keyword": "?0" } }
                  ]
                }
              }
            ]
          }
        }
        """)
    Optional<Boolean> existsMatchBetweenProfiles(String profileId1, String profileId2);
}
