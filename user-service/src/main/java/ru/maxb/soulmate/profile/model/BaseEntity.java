package ru.maxb.soulmate.profile.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @Column(name = "active", nullable = false)
    private boolean active;

    @NotAudited
    @CreatedDate
    @Column(name = "created", nullable = false)
    private Instant created;

    @NotAudited
    @LastModifiedDate
    @Column(name = "updated", nullable = false)
    private Instant updated;

}