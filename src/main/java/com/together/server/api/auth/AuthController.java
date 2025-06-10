package com.together.server.api.auth;

import com.together.server.application.auth.AuthService;
import com.together.server.application.auth.KakaoService;
import com.together.server.application.auth.request.LoginRequest;
import com.together.server.application.auth.request.RegisterRequest;
import com.together.server.application.auth.response.KakaoUserResponse;
import com.together.server.application.auth.response.TokenResponse;
import com.together.server.application.member.response.MemberInfoResponse;
import com.together.server.infra.oauth.KakaoOAuthClient;
import com.together.server.infra.web.TokenCookieHandler;
import com.together.server.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenCookieHandler tokenCookieHandler;

    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoService kakaoService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인 처리 API")
    public ResponseEntity<ApiResponse<Void>> login(@RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);

        ResponseCookie accessTokenCookie = tokenCookieHandler.createAccessTokenCookie(response.accessToken());
        ResponseCookie refreshTokenCookie = tokenCookieHandler.createRefreshTokenCookie(response.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success());
    }

    @GetMapping("/login/kakao")
    @Operation(summary = "카카오 로그인", description = "사용자 카카오 로그인으로 JWT 토큰 발급")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoLogin(@RequestParam String code, HttpServletResponse response) {
        String kakaoAccessToken = kakaoOAuthClient.getAccessToken(code);
        KakaoUserResponse kakaoUser = kakaoService.getKakaoUser(kakaoAccessToken);
        TokenResponse token = authService.kakaoLogin(kakaoUser);

        ResponseCookie accessTokenCookie = tokenCookieHandler.createAccessTokenCookie(token.accessToken());
        ResponseCookie refreshTokenCookie = tokenCookieHandler.createRefreshTokenCookie(token.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(token));
    }


    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "사용자 회원가입 처리 API")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> register(@RequestBody RegisterRequest request) {
        MemberInfoResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃 처리 API")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue("access_token") String accessToken) {
        authService.logout(accessToken);

        ResponseCookie expiredAccessTokenCookie = tokenCookieHandler.createExpiredAccessTokenCookie();
        ResponseCookie expiredRefreshTokenCookie = tokenCookieHandler.createExpiredRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString(), expiredRefreshTokenCookie.toString())
                .body(ApiResponse.success());
    }

    @PostMapping("/reissue")
    @Operation(summary = "AccessToken 재발급", description = "Refresh Token 기반으로 Access Token 재발급 API")
    public ResponseEntity<ApiResponse<Void>> reissue(@CookieValue("refresh_token") String refreshToken) {
        TokenResponse newToken = authService.reissue(refreshToken);

        ResponseCookie newAccessTokenCookie = tokenCookieHandler.createAccessTokenCookie(newToken.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessTokenCookie.toString())
                .body(ApiResponse.success());
    }
}
