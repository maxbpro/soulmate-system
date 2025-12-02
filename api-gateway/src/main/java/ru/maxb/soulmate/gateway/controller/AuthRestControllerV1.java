package ru.maxb.soulmate.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.maxb.soulmate.gateway.api.AuthApi;
import ru.maxb.soulmate.gateway.dto.GatewayRegistrationRequestDto;
import ru.maxb.soulmate.gateway.dto.TokenRefreshRequest;
import ru.maxb.soulmate.gateway.dto.TokenResponse;
import ru.maxb.soulmate.gateway.dto.UserInfoResponse;
import ru.maxb.soulmate.gateway.dto.UserLoginRequest;
import ru.maxb.soulmate.gateway.service.UserService;
import ru.maxb.soulmate.gateway.service.TokenService;


@RestController
@RequiredArgsConstructor
public class AuthRestControllerV1 implements AuthApi {

    private final TokenService tokenService;
    private final UserService userService;

    @Override
    public ResponseEntity<TokenResponse> login(UserLoginRequest userLoginRequest) {
        return ResponseEntity.ok(tokenService.login(userLoginRequest));
    }

    @Override
    public ResponseEntity<TokenResponse> refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        return ResponseEntity.ok(tokenService.refreshToken(tokenRefreshRequest));
    }

    @Override
    public ResponseEntity<TokenResponse> registration(MultipartFile imageFile,
                                                      GatewayRegistrationRequestDto profileRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body( userService.register(imageFile, profileRequestDto));
    }

    @GetMapping("/v1/auth/me")
    public ResponseEntity<UserInfoResponse> getMe() {
        return ResponseEntity.ok(userService.getUserInfo());
    }

}
