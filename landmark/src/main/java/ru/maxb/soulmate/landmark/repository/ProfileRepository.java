package ru.maxb.soulmate.landmark.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.maxb.soulmate.landmark.model.Profile;

public interface ProfileRepository extends ElasticsearchRepository<Profile, String> {

}
