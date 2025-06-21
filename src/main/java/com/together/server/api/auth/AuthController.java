package com.together.server.api.auth;

import com.together.server.application.auth.AuthService;
import com.together.server.application.auth.KakaoService;
import com.together.server.application.auth.request.FirstLoginRequest;
import com.together.server.application.auth.request.KakaoCodeRequest;
import com.together.server.application.auth.request.LoginRequest;
import com.together.server.application.auth.request.RegisterRequest;
import com.together.server.application.auth.response.*;
import com.together.server.application.member.response.MemberInfoResponse;
import com.together.server.infra.oauth.KakaoOAuthClient;
import com.together.server.infra.security.Accessor;
import com.together.server.infra.web.TokenCookieHandler;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import com.together.server.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<ApiResponse<LoginViewResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        ResponseCookie accessTokenCookie = tokenCookieHandler.createAccessTokenCookie(response.accessToken());
        ResponseCookie refreshTokenCookie = tokenCookieHandler.createRefreshTokenCookie(response.refreshToken());

        System.out.println("accessToken: " + response.accessToken());
        System.out.println("refreshToken: " + response.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(new LoginViewResponse(response.isFirstLogin())));
    }

    @PatchMapping("/firstLogin")
    @Operation(summary = "최초 로그인 추가 정보 입력", description = "최초 로그인 시 추가 정보 등록")
    public ResponseEntity<ApiResponse<Void>> updateFirstLoginInfo(@RequestBody FirstLoginRequest request,
                                                                  @AuthenticationPrincipal Accessor accessor) {
        if (accessor.isGuest()) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }
        String memberId = accessor.id();
        authService.updateFirstLoginInfo(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/login/kakao")
    @Operation(summary = "카카오 로그인", description = "클라이언트에서 받은 인가코드로 JWT 발급")
    public ResponseEntity<ApiResponse<LoginViewResponse>> kakaoLogin(
            @RequestBody KakaoCodeRequest request,
            HttpServletResponse response
    ) {
        String kakaoAccessToken = kakaoOAuthClient.getAccessToken(request.code());
        KakaoUserResponse kakaoUser = kakaoService.getKakaoUser(kakaoAccessToken);
        KakaoLoginResponse kakaoLogin = authService.kakaoLogin(kakaoUser);

        ResponseCookie accessTokenCookie = tokenCookieHandler.createAccessTokenCookie(kakaoLogin.accessToken());
        ResponseCookie refreshTokenCookie = tokenCookieHandler.createRefreshTokenCookie(kakaoLogin.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(new LoginViewResponse(kakaoLogin.isFirstLogin())));
    }


    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "사용자 회원가입 처리 API")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> register(@RequestBody RegisterRequest request) {
        MemberInfoResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃 처리 API")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue("access_token") String accessToken) {
        authService.logout(accessToken);

        ResponseCookie expiredAccessTokenCookie = tokenCookieHandler.createExpiredAccessTokenCookie();
        ResponseCookie expiredRefreshTokenCookie = tokenCookieHandler.createExpiredRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString(), expiredRefreshTokenCookie.toString())
                .body(ApiResponse.success(null));
    }

    @PostMapping("/reissue")
    @Operation(summary = "AccessToken 재발급", description = "Refresh Token 기반으로 Access Token 재발급 API")
    public ResponseEntity<ApiResponse<Void>> reissue(@CookieValue("refresh_token") String refreshToken) {
        TokenResponse newToken = authService.reissue(refreshToken);

        ResponseCookie newAccessTokenCookie = tokenCookieHandler.createAccessTokenCookie(newToken.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessTokenCookie.toString())
                .body(ApiResponse.success(null));
    }
}
