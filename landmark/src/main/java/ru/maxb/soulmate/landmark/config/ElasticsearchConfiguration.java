package ru.maxb.soulmate.landmark.config;

import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@EnableElasticsearchRepositories(basePackages = "ru.maxb.soulmate.landmark.repository")
public class ElasticsearchConfiguration {
}
