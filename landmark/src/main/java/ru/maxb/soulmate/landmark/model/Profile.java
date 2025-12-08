package ru.maxb.soulmate.landmark.model;

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
@Document(indexName = "profile")
public class Profile {

    @Id
    private String id;

    private String profileId;
    private String landmarks;
    private GeoPoint location;
    private Gender gender;
    private Integer ageMin;
    private Integer ageMax;
    private Integer radius;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate dateOfBirth;

}
