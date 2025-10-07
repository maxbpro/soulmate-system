package ru.maxb.soulmate.swipe.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.maxb.soulmate.swipe.model.ProfileEntity;

import java.util.UUID;

@Repository
public interface ProfileRepository extends CrudRepository<ProfileEntity, UUID> {
}
