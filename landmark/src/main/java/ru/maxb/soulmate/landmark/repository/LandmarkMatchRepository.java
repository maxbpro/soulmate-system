package ru.maxb.soulmate.landmark.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;

public interface LandmarkMatchRepository extends ElasticsearchRepository<LandmarkMatch, String> {

}
