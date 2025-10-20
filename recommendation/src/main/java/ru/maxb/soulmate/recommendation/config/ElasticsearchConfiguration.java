package ru.maxb.soulmate.recommendation.config;

import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@EnableElasticsearchRepositories(basePackages = "ru.maxb.soulmate.recommendation.repository")
public class ElasticsearchConfiguration {
}
