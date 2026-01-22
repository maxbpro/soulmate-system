package ru.maxb.soulmate.landmark.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.maxb.soulmate.landmark.model.Profile;

import java.util.Optional;

public interface ProfileRepository extends ElasticsearchRepository<Profile, String> {

    Optional<Profile> findByProfileId(String profileId);
}
