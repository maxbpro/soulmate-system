package ru.maxb.soulmate.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.landmark.model.Gender;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class LandmarkReadService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<LandmarkMatch> findByParams(double lat, double lon, int distance,
                                            int ageMin, int ageMax, Gender interestedIn,
                                            UUID excludeProfileId) {
        IndexCoordinates index = IndexCoordinates.of("landmark_match");

        LocalDate upperDate = LocalDate.now().minusYears(ageMin);
        LocalDate lowerDate = LocalDate.now().minusYears(ageMax);

        CriteriaQuery criteriaQuery = new CriteriaQuery(
                Criteria.and()
                        .and(Criteria.where("location").within(new GeoPoint(lat, lon), distance + "km"))
                        .and(Criteria.where("gender").is(interestedIn))
                        .and(Criteria.where("soulmateId").is(excludeProfileId))
                        .and(Criteria.where("profileId").notIn(excludeProfileId))
                        .and(new Criteria("dateOfBirth").between(lowerDate, upperDate))
        );
        //criteriaQuery.setPageable(PageRequest.of(page, pageSize));

        SearchHits<LandmarkMatch> searchHits = elasticsearchOperations.search(criteriaQuery, LandmarkMatch.class, index);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

}
