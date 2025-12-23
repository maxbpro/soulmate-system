package ru.maxb.soulmate.profile.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

    @NotNull
    @ColumnDefault("true")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @NotNull
    @ColumnDefault("(now) AT TIME ZONE 'utc'::text")
    @Column(name = "created", nullable = false)
    private Instant created;

    @NotNull
    @ColumnDefault("(now) AT TIME ZONE 'utc'::text")
    @Column(name = "updated", nullable = false)
    private Instant updated;

}