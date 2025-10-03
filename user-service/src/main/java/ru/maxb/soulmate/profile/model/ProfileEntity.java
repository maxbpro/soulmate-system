package ru.maxb.soulmate.profile.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.validation.constraints.Size;


@Setter
@Getter
@Entity
@Audited
@Table(name = "profiles", schema = "profile")
public class ProfileEntity extends BaseEntity {

    @Size(max = 1024)
    @Column(name = "email", nullable = false, unique = true, length = 1024)
    private String email;

    @Size(max = 64)
    @Column(name = "first_name", nullable = false, unique = true, length = 64)
    private String firstName;

    @Size(max = 64)
    @Column(name = "last_name", nullable = false, unique = true, length = 64)
    private String lastName;
}
