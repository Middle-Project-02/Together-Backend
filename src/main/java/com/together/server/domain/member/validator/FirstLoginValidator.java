package com.together.server.domain.member.validator;

import com.together.server.application.auth.request.FirstLoginRequest;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FirstLoginValidator {

    public void validate(FirstLoginRequest request) {
        if (request.fontMode() == null) {
            throw new CoreException(ErrorType.REQUIRED_FONT_MODE);
        }
    }
}