package ru.maxb.soulmate.profile.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.validation.constraints.Size;
import java.time.LocalDate;


@Setter
@Getter
@Entity
@Audited
@Table(name = "profiles", schema = "profile")
public class ProfileEntity extends BaseEntity {

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

    @Column(name = "radius", nullable = false)
    private int radius;

    @Column(name = "age_min", nullable = false)
    private int ageMin;

    @Column(name = "age_max", nullable = false)
    private int ageMax;
}
