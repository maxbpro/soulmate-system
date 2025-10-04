package ru.maxb.soulmate.gateway.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
    public Mono<ResponseEntity<TokenResponse>> login(@Valid @RequestBody Mono<UserLoginRequest> body) {
        return body.flatMap(tokenService::login)
                .map(ResponseEntity::ok);
    }


    @PostMapping(value = "/refresh-token")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@Valid @RequestBody Mono<TokenRefreshRequest> body) {
        return body.flatMap(tokenService::refreshToken).map(ResponseEntity::ok);
    }

    @PostMapping(value = "/registration")
    public Mono<ResponseEntity<TokenResponse>> registration(@Valid @RequestBody Mono<GatewayRegistrationRequestDto> body) {
        return body.flatMap(userService::register).map(t -> ResponseEntity.status(HttpStatus.CREATED).body(t));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserInfoResponse>> getMe() {
        return userService.getUserInfo()
                .map(ResponseEntity::ok);
    }

}
