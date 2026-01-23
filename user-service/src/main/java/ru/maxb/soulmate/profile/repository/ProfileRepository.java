package ru.maxb.soulmate.profile.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.maxb.soulmate.profile.model.ProfileEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends CrudRepository<ProfileEntity, UUID> {

    @Modifying
    @Query("""
            UPDATE ProfileEntity p SET p.active = false WHERE p.id = :id
            """)
    void softDelete(@Param("id") UUID id);

    @Query("SELECT p FROM ProfileEntity p WHERE p.active = true")
    Optional<ProfileEntity> findById(UUID id);
}
