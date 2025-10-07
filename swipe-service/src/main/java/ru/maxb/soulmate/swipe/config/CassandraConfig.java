package ru.maxb.soulmate.swipe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "ru.maxb.soulmate.swipe.repository")
public class CassandraConfig {
}
