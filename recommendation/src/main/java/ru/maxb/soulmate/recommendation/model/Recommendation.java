package ru.maxb.soulmate.recommendation.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Getter
@Setter
@Document(indexName = "recommendation")
public class Recommendation {

    @Id
    private String id;
    private String title;
    private GeoPoint location;

    //todo
    private String[] landmark;
}
