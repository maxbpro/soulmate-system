package ru.maxb.soulmate.profile.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Setter
@Getter
@Entity
@Audited
@Table(name = "profiles", schema = "profile")
public class ProfileEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 64)
    @Column(name = "email", nullable = false, unique = true, length = 64)
    private String email;

    @Size(max = 16)
    @Column(name = "phone_number", nullable = false, unique = true, length = 16)
    private String phoneNumber;

    @Size(max = 64)
    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;

    @Size(max = 64)
    @Column(name = "last_name", nullable = false, length = 64)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "interested_in", nullable = false, length = 64)
    private Gender interestedIn;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 64)
    private Gender gender;

    @Column(name = "radius", nullable = false)
    private int radius;

    @Column(name = "age_min", nullable = false)
    private int ageMin;

    @Column(name = "age_max", nullable = false)
    private int ageMax;

    @Column(name = "photos", columnDefinition = "VARCHAR(64)[]")
    private List<String> photos = new ArrayList<>();

    @Column(name = "landmarks")
    private String landmarks;
}
