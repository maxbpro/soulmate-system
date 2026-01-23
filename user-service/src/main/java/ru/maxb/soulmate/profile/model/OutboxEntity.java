package ru.maxb.soulmate.profile.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(schema = "profile", name = "outbox")
@EntityListeners(AuditingEntityListener.class)
public class OutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregatetype")
    private String aggregateType;

    @Column(name = "aggregateid")
    private String aggregateId;

    @Enumerated(EnumType.STRING)
    private OutboxType type;

    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;

    @CreatedDate
    @Column(name = "created", nullable = false)
    private Instant created;
}
