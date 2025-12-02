package ru.maxb.soulmate.gateway.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.TokenRefreshRequest;
import ru.maxb.soulmate.gateway.dto.TokenResponse;
import ru.maxb.soulmate.gateway.dto.UserInfoResponse;
import ru.maxb.soulmate.gateway.dto.UserLoginRequest;
import ru.maxb.soulmate.gateway.service.UserService;
import ru.maxb.soulmate.gateway.service.TokenService;


@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1/auth")
public class AuthRestControllerV1 {

    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping(value = "/login")
    public TokenResponse login(@Valid @RequestBody UserLoginRequest request) {
        return tokenService.login(request);
    }


    @PostMapping(value = "/refresh-token")
    public TokenResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return tokenService.refreshToken(request);
    }

    @PostMapping(value = "/registration")
    public ResponseEntity<TokenResponse> registration(@Valid @RequestBody GatewayRegistrationRequestDto request) {
        TokenResponse tokenResponse = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }

    @GetMapping("/me")
    public UserInfoResponse getMe() {
        return userService.getUserInfo();
    }

}
