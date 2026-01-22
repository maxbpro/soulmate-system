package ru.maxb.soulmate.gateway.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/auth")
public class AuthRestControllerV1 {

    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@Valid @RequestBody Mono<UserLoginRequest> userLoginRequest) {
        return userLoginRequest.flatMap(tokenService::login).map(ResponseEntity::ok);
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@Valid @RequestBody Mono<TokenRefreshRequest> tokenRefreshRequest) {
        return tokenRefreshRequest
                .flatMap(tokenService::refreshToken)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/registration")
    public Mono<ResponseEntity<TokenResponse>> registration(@Valid @RequestBody Mono<GatewayRegistrationRequestDto> profileRequestDto) {
        return profileRequestDto.flatMap(userService::register)
                .map(v -> ResponseEntity.status(HttpStatus.CREATED).body(v));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserInfoResponse>> getMe() {
        return userService.getUserInfo().map(ResponseEntity::ok);
    }

}
