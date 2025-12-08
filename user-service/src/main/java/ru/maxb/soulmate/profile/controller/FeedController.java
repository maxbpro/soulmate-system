package ru.maxb.soulmate.profile.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.maxb.soulmate.profile.service.FeedService;
import ru.maxb.soulmate.user.api.FeedApi;
import ru.maxb.soulmate.user.dto.ProfileDto;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedController implements FeedApi {

    private final FeedService feedService;

    @Override
    public ResponseEntity<List<ProfileDto>> getFeed(@NotNull @Valid BigDecimal lat,
                                                    @NotNull @Valid BigDecimal lng) {


        UUID profileId = null;

        return ResponseEntity.ok(feedService.getProfiles());
    }

//    private void getSecurity(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
//            Jwt jwt = (Jwt) authentication.getPrincipal();
//            String preferredUsername = jwt.getClaimAsString("preferred_username");
//            String firstName = jwt.getClaimAsString("given_name"); // or "firstName" depending on Keycloak mapping
//            String lastName = jwt.getClaimAsString("family_name"); // or "lastName" depending on Keycloak mapping
//
//            return "Preferred Username: " + preferredUsername +
//                    ", First Name: " + firstName +
//                    ", Last Name: " + lastName;
//        }
//        return "JWT token not found or user not authenticated.";
//    }
}
