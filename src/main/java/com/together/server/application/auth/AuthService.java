package com.together.server.application.auth;

import com.together.server.application.auth.exception.MemberNotFoundException;
import com.together.server.application.auth.request.LoginRequest;
import com.together.server.application.auth.request.RegisterRequest;
import com.together.server.application.auth.response.MemberDetailsResponse;
import com.together.server.application.auth.response.TokenResponse;
import com.together.server.domain.member.Member;
import com.together.server.domain.member.MemberRepository;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public MemberDetailsResponse getMemberDetails(String id) {
        Member member = memberRepository
                .findById(id)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 사용자입니다. 사용자 식별자: %s".formatted(id)));

        return new MemberDetailsResponse(member.getId(), member.getEmail(), member.getNickname(), member.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Member member = getByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CoreException(ErrorType.MEMBER_PASSWORD_MISMATCH);
        }

        String token = tokenProvider.createToken(member.getId());
        return new TokenResponse(token);
    }

    @Transactional
    public String register(RegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CoreException(ErrorType.MEMBER_USERNAME_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = new Member(request.email(), request.nickname(), encodedPassword);
        Member savedMember = memberRepository.save(member);

        return savedMember.getId();
    }

    private Member getByEmail(String email) {
        return memberRepository
                .findByEmail(email)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
    }
}
