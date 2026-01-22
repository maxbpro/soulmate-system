package ru.maxb.soulmate.landmark.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDate;

@Getter
@Setter
@Document(indexName = "landmark_match")
@EqualsAndHashCode
public class LandmarkMatch {

    @Id
    private String id;

    private String profileId;
    private String landmarksOfProfileId;

    private String soulmateId;
    private String landmarksOfSoulmateId;

    //distance between landmarks
    private double distance;

    private GeoPoint location;
    private Gender gender;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate dateOfBirth;
}
