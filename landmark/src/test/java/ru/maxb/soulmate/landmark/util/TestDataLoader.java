package ru.maxb.soulmate.landmark.util;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import ru.maxb.soulmate.landmark.model.LandmarkMatch;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TestDataLoader {

    private final ElasticsearchOperations elasticsearchOperations;

    @SneakyThrows
    public String loadResourceAsString(String resourcePath) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    public List<LandmarkMatch> findAllMatches() {
        return elasticsearchOperations.search(
                        org.springframework.data.elasticsearch.core.query.Query.findAll(),
                        LandmarkMatch.class
                ).stream()
                .map(SearchHit::getContent)
                .toList();
    }

//    @SneakyThrows
//    public List<String> loadMultipleProfileEvents(int count) {
//        // Generate or load multiple test events
//        // For simplicity, load same event but with different IDs
//        List<String> events = new ArrayList<>();
//        String template = loadResourceAsString("debezium/profile_created.json");
//
//        String aggregateid = UUID.randomUUID().toString();
//
//        for (int i = 0; i < count; i++) {
//            String event = template.replace("${aggregateid}", aggregateid)
//                    .replace("${id}", aggregateid);
//            events.add(event);
//        }
//        return events;
//    }

    public String getEvent(String aggregateid){
        String template = loadResourceAsString("debezium/profile_created_template.json");
        String event = template.replace("${aggregateid}", aggregateid)
                .replace("${id}", aggregateid);
        return event;
    }

}
