package com.together.server.application.auth;

import com.together.server.application.auth.request.FirstLoginRequest;
import com.together.server.application.auth.request.LoginRequest;
import com.together.server.application.auth.request.RegisterRequest;
import com.together.server.application.auth.response.*;
import com.together.server.application.member.response.MemberInfoResponse;
import com.together.server.domain.member.Member;
import com.together.server.domain.member.MemberRepository;
import com.together.server.domain.member.validator.FirstLoginValidator;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import com.together.server.domain.member.validator.RegisterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RegisterValidator registerValidator;
    private final FirstLoginValidator firstLoginValidator;

    @Transactional(readOnly = true)
    public MemberDetailsResponse getMemberDetails(String id) {
        Member member = memberRepository
                .findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        return new MemberDetailsResponse(member.getId(), member.getMemberId(), member.getNickname(), member.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Member member = getByMemberId(request.memberId());

        if (!memberRepository.existsByMemberId(request.memberId())) {
            throw new CoreException(ErrorType.INVALID_PHONE_OR_PASSWORD);
        }

        if (member.isDeleted()) {
            throw new CoreException(ErrorType.MEMBER_WITHDRAWN);
        }

        if (member.getPassword().equals("KAKAO_PASSWORD")) {
            throw new CoreException(ErrorType.SOCIAL_LOGIN_ONLY);
        }

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CoreException(ErrorType.INVALID_PHONE_OR_PASSWORD);
        }

        String accessToken = tokenProvider.createToken(member.getId().toString());
        String refreshToken = tokenProvider.createRefreshToken(member.getId().toString());

        return new LoginResponse(accessToken, refreshToken, member.getIsFirstLogin());
    }

    @Transactional
    public MemberInfoResponse register(RegisterRequest request) {
        registerValidator.validate(request);

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = new Member(request.memberId(), request.nickname(), encodedPassword);
        Member savedMember = memberRepository.save(member);

        return new MemberInfoResponse(
                savedMember.getId(),
                savedMember.getMemberId(),
                savedMember.getNickname(),
                savedMember.getCreatedAt()
        );
    }

    private Member getByMemberId(String memberId) {
        return memberRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
    }

    @Transactional
    public KakaoLoginResponse kakaoLogin(KakaoUserResponse kakaoUser) {
        if (kakaoUser.email() == null || kakaoUser.nickname() == null) {
            throw new CoreException(ErrorType.KAKAO_USERINFO_INCOMPLETE);
        }

        Optional<Member> optionalMember = memberRepository.findByMemberId(kakaoUser.email());

        Member member = optionalMember.orElseGet(() -> {
            Member newMember = new Member(
                    kakaoUser.email(),
                    kakaoUser.nickname(),
                    "KAKAO_PASSWORD"
            );
            return memberRepository.save(newMember);
        });

        if (member.isDeleted()) {
            throw new CoreException(ErrorType.MEMBER_WITHDRAWN);
        }

        String accessToken = tokenProvider.createToken(member.getId().toString());
        String refreshToken = tokenProvider.createRefreshToken(member.getId().toString());
        return new KakaoLoginResponse(accessToken, refreshToken, member.getIsFirstLogin());
    }

    @Transactional
    public void logout(String accessToken) {
        System.out.println("AccessToken 로그아웃 요청: " + accessToken);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new CoreException(ErrorType.REFRESH_TOKEN_INVALID);
        }
        String memberId = tokenProvider.getMemberId(refreshToken);
        String newAccessToken = tokenProvider.createToken(memberId);

        return new TokenResponse(newAccessToken, refreshToken);
    }

    @Transactional
    public void updateFirstLoginInfo(String memberId, FirstLoginRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        if (!member.getIsFirstLogin()) {
            throw new CoreException(ErrorType.ALREADY_UPDATED_FIRST_INFO);
        }
        firstLoginValidator.validate(request);

        member.updateFirstLoginInfo(request.fontMode());
    }

}
