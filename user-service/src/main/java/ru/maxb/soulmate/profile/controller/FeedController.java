package ru.maxb.soulmate.profile.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.profile.service.FeedService;
import ru.maxb.soulmate.profile.util.SecurityUtils;
import ru.maxb.soulmate.user.api.FeedApi;
import ru.maxb.soulmate.user.dto.ProfileDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedController implements FeedApi {

    private final FeedService feedService;
    private final SecurityUtils securityUtils;

    @Override
    public ResponseEntity<List<ProfileDto>> getFeed(@NotNull @DecimalMin("-90") @DecimalMax("90") @Valid Double lat,
                                                    @NotNull @DecimalMin("-180") @DecimalMax("180") @Valid Double lng,
                                                    @NotNull @Valid Integer page,
                                                    @NotNull @Valid Integer pageNumber) {
        UUID profileId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(feedService.getProfiles(profileId, lat, lng, page, pageNumber));
    }
}
