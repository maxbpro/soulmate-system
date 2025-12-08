package ru.maxb.soulmate.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.landmark.model.Gender;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;
import ru.maxb.soulmate.landmark.model.Profile;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class LandmarkReadService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<LandmarkMatch> findByParams( double lat, double lon, int distance,
                                            int ageMin, int ageMax, Gender interestedIn,
                                             String excludeProfileId) {
        IndexCoordinates index = IndexCoordinates.of("landmark_match");

        String queryString = """
                {
                    "geo_distance":{
                      "location":{
                          "lat":%.3f,
                          "lon":%.3f
                      },
                      "distance":"%d"
                    },
                    "gender":"%s",
                    "range": {
                        "birthday": {
                          "gte": "now-31y/d",
                          "lt": "now-25y/d"
                        }
                    }
                }
                """.formatted(lat, lon, distance, interestedIn);

        //need to use location

//        CriteriaQuery criteriaQuery = new CriteriaQuery(
//                new Criteria("location").within(new GeoPoint(lat, lon), distance + "km")
//        );

        CriteriaQuery criteriaQuery = new CriteriaQuery(
                Criteria.and()
                        .and(Criteria.where("location").within(new GeoPoint(lat, lon), distance + "km"))
                        .and(Criteria.where("gender").is(interestedIn))
                        .and(Criteria.where("soulmateId").is(excludeProfileId))
                        .and(Criteria.where("profileId").notIn(excludeProfileId))
        );
        //criteriaQuery.setPageable(PageRequest.of(page, pageSize));

        SearchHits<LandmarkMatch> searchHits = elasticsearchOperations.search(criteriaQuery, LandmarkMatch.class, index);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

}
