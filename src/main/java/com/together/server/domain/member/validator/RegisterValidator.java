package com.together.server.domain.member.validator;

import com.together.server.application.auth.request.RegisterRequest;
import com.together.server.domain.member.MemberRepository;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterValidator {

    private final MemberRepository memberRepository;

    public void validate(RegisterRequest request) {
        if (request.memberId() == null || request.memberId().trim().isEmpty()) {
            throw new CoreException(ErrorType.REQUIRED_MEMBER_ID);
        }
        if (request.nickname() == null || request.nickname().trim().isEmpty()) {
            throw new CoreException(ErrorType.REQUIRED_NICKNAME);
        }
        if (request.password() == null || request.password().trim().isEmpty()) {
            throw new CoreException(ErrorType.REQUIRED_PASSWORD);
        }

        if (!request.memberId().matches("^01[016789]\\d{7,8}$")) {
            throw new CoreException(ErrorType.INVALID_PHONE_NUMBER_FORMAT);
        }

        if (memberRepository.existsByMemberId(request.memberId())) {
            throw new CoreException(ErrorType.MEMBER_PHONE_NUMBER_ALREADY_EXISTS);
        }

    }
}
