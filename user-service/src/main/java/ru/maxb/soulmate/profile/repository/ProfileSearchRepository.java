package ru.maxb.soulmate.profile.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.maxb.soulmate.profile.model.ProfileSearch;

public interface ProfileSearchRepository extends ElasticsearchRepository<ProfileSearch, String> {

}
