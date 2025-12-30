package ru.maxb.soulmate.swipe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.maxb.soulmate.swipe.dto.SwipeRequestDto;
import ru.maxb.soulmate.swipe.service.SwipeService;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SwipeController.class)
class SwipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SwipeService swipeService;

    private final UUID profileId = UUID.randomUUID();

    private final Jwt jwt = Jwt.withTokenValue("mock-token")
            .header("alg", "RS256")
            .claim("sub", profileId.toString())
            //.claim("scope", "read")
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

    @Test
    void createSwipe() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(getSwipeRequestDto());

        mockMvc.perform(post("/api/v1/swipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt))
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void getSwipes() throws Exception {
        mockMvc.perform(get("/api/v1/swipes")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)))
                .andExpect(status().isOk());
    }

    private SwipeRequestDto getSwipeRequestDto() {
        SwipeRequestDto swipeRequestDto = new SwipeRequestDto();
        swipeRequestDto.setSwipedUserId(UUID.randomUUID());
        swipeRequestDto.setLiked(true);
        return swipeRequestDto;
    }
}