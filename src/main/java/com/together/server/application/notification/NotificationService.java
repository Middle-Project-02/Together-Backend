package com.together.server.application.notification;

import com.together.server.application.notification.request.NotificationCreateRequest;
import com.together.server.application.notification.response.NotificationDetailResponse;
import com.together.server.application.notification.response.NotificationSimpleResponse;
import com.together.server.domain.notification.Notification;
import com.together.server.domain.notification.NotificationRepository;
import com.together.server.domain.notification.validator.NotificationCreateValidator;
import com.together.server.support.error.CoreException;
import com.together.server.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationCreateValidator notificationCreateValidator;

    @Transactional
    public void createNotification(NotificationCreateRequest request) {
        notificationCreateValidator.validate(request);
        Notification notification = new Notification(request.title(), request.content());
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationSimpleResponse> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(n -> new NotificationSimpleResponse(n.getId(), n.getTitle(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotificationDetailResponse getNotificationDetail(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOTIFICATION_NOT_FOUND));

        return new NotificationDetailResponse(n.getId(), n.getTitle(), n.getContent(), n.getCreatedAt());
    }
}
