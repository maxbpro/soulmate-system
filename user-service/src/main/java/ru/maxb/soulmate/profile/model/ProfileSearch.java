package ru.maxb.soulmate.profile.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;


@Getter
@Setter
@Document(indexName = "profile")
public class ProfileSearch {

    @Id
    private String id;
    private String userId;
    private String swipedUserId;
    private GeoPoint location;
    //todo
    private String[] landmark;
}
