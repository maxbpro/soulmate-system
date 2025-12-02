package ru.maxb.soulmate.recommendation.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Getter
@Setter
@Document(indexName = "profile")
public class Profile {

    @Id
    private String id;
    private String userId;
    private GeoPoint swipedUserId;
}
