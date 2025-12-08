package ru.maxb.soulmate.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import ru.maxb.soulmate.landmark.model.Profile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileReadService {

    private final ElasticsearchOperations elasticsearchOperations;

    public long getCount() {
        IndexCoordinates index = IndexCoordinates.of("profile");
        CriteriaQuery criteriaQuery = new CriteriaQuery(Criteria.and());

        return elasticsearchOperations.count(criteriaQuery, index);
    }

    public List<Profile> searchAll(int page, int pageSize) {
        IndexCoordinates index = IndexCoordinates.of("profile");

        CriteriaQuery criteriaQuery = new CriteriaQuery(Criteria.and());
        criteriaQuery.setPageable(PageRequest.of(page, pageSize));

        SearchHits<Profile> searchHits = elasticsearchOperations.search(criteriaQuery, Profile.class, index);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
