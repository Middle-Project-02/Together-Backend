package com.together.server.domain.notification.validator;

import com.together.server.application.notification.request.NotificationCreateRequest;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class NotificationCreateValidator {

    public void validate(NotificationCreateRequest request) {
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new CoreException(ErrorType.INVALID_NOTIFICATION_TITLE);
        }

        if (request.issue() == null || request.issue().trim().isEmpty() || request.solution() == null || request.solution().trim().isEmpty()) {
            throw new CoreException(ErrorType.INVALID_NOTIFICATION_ISSUE_OR_SOLUTION);
        }
    }
}
