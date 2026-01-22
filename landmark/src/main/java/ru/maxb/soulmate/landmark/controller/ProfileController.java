package ru.maxb.soulmate.landmark.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.landmark.api.ProfileApi;
import ru.maxb.soulmate.landmark.service.ProfileService;
import ru.maxb.soulmate.landmark.util.SecurityUtils;

@RestController
@RequiredArgsConstructor
public class ProfileController implements ProfileApi {

    private final ProfileService profileService;
    private final SecurityUtils securityUtils;

    @Override
    public ResponseEntity<Void> updateProfileLocation(@NotNull @Valid Double lat,
                                                      @NotNull @Valid Double lng) {
        profileService.updateLocation(securityUtils.getCurrentUserId(), lat, lng);
        return ResponseEntity.ok().build();
    }
}
