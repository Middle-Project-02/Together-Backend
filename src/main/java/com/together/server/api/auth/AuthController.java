package com.together.server.api.auth;

import com.together.server.application.auth.AuthService;
import com.together.server.application.auth.request.LoginRequest;
import com.together.server.application.auth.request.RegisterRequest;
import com.together.server.application.auth.response.TokenResponse;
import com.together.server.infra.web.TokenCookieHandler;
import com.together.server.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenCookieHandler tokenCookieHandler;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인 처리 API")
    public ResponseEntity<ApiResponse<Void>> login(@RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        ResponseCookie accessTokenCookie = tokenCookieHandler.createAccessTokenCookie(response.token());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body(ApiResponse.success());
    }

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "사용자 회원가입 처리 API")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃 처리 API")
    public ResponseEntity<ApiResponse<Void>> logout() {
        ResponseCookie expiredAccessTokenCookie = tokenCookieHandler.createExpiredAccessTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString())
                .body(ApiResponse.success());
    }
}
