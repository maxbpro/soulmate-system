package ru.maxb.soulmate.swipe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.maxb.soulmate.swipe.dto.SwipeDto;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.exception.SwipeException;
import ru.maxb.soulmate.swipe.service.SwipeService;
import ru.maxb.soulmate.swipe.util.SecurityUtils;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SwipeController.class)
class SwipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SwipeService swipeService;

    @MockitoBean
    private SecurityUtils securityUtils;

    private final UUID profileId = UUID.randomUUID();

    private UUID currentUserId;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();

        jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("sub", currentUserId.toString())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(securityUtils.getCurrentUserId()).thenReturn(currentUserId);
    }

    @Test
    void createSwipe() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        SwipeRequestDto request = getSwipeRequestDto(targetUserId);

        SwipeDto expectedResponse = new SwipeDto()
                .userId(currentUserId)
                .swipedUserId(targetUserId)
                .liked(true);

        when(swipeService.createSwipe(currentUserId, request))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/swipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(currentUserId.toString()))
                .andExpect(jsonPath("$.swipedUserId").value(targetUserId.toString()))
                .andExpect(jsonPath("$.liked").value(true));

        verify(swipeService).createSwipe(eq(currentUserId), any(SwipeRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/swipes - Should return 400 for self-swipe")
    void createSwipe_SelfSwipe_ShouldReturnBadRequest() throws Exception {
        // Given - Self swipe
        SwipeRequestDto selfSwipeRequest = getSwipeRequestDto(currentUserId);

        when(swipeService.createSwipe(eq(currentUserId), any(SwipeRequestDto.class)))
                .thenThrow(new SwipeException("Cannot swipe on yourself"));

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/swipes")
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selfSwipeRequest)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot swipe on yourself")));

        verify(swipeService).createSwipe(eq(currentUserId), any(SwipeRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/swipes - Should return 401 when unauthenticated")
    void createSwipe_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        SwipeRequestDto selfSwipeRequest = getSwipeRequestDto(currentUserId);
        // When
        ResultActions result = mockMvc.perform(post("/api/v1/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selfSwipeRequest)));

        // Then
        result.andExpect(status().isForbidden());
        verify(swipeService, never()).createSwipe(any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/swipes - Should return 409 for duplicate swipe")
    void createSwipe_DuplicateSwipe_ShouldReturnConflict() throws Exception {
        SwipeRequestDto selfSwipeRequest = getSwipeRequestDto(currentUserId);

        // Given
        when(swipeService.createSwipe(eq(currentUserId), any(SwipeRequestDto.class)))
                .thenThrow(new SwipeException("Already swiped on this user"));

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/swipes")
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(selfSwipeRequest)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Already swiped on this user")));

        verify(swipeService).createSwipe(eq(currentUserId), any(SwipeRequestDto.class));
    }

    @Test
    void getSwipes() throws Exception {
        mockMvc.perform(get("/api/v1/swipes")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/swipes - Should handle service exception")
    void getSwipes_ServiceError_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(swipeService.getSwipes(currentUserId))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/swipes")
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)));

        // Then
        result.andExpect(status().isInternalServerError());

        verify(swipeService).getSwipes(currentUserId);
    }

    @Test
    void getSwipesWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/swipes"))
                .andExpect(status().isUnauthorized());
    }

    private SwipeRequestDto getSwipeRequestDto(UUID targetUserId) {
        SwipeRequestDto swipeRequestDto = new SwipeRequestDto();
        swipeRequestDto.setSwipedUserId(targetUserId);
        swipeRequestDto.setLiked(true);
        return swipeRequestDto;
    }
}