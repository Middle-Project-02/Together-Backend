package com.together.server.application.notification;

import com.together.server.application.notification.request.NotificationCreateRequest;
import com.together.server.application.notification.response.NotificationDetailResponse;
import com.together.server.application.notification.response.NotificationSimpleResponse;
import com.together.server.domain.member.Member;
import com.together.server.domain.member.MemberRepository;
import com.together.server.domain.notification.Notification;
import com.together.server.domain.notification.NotificationRepository;
import com.together.server.domain.notification.validator.NotificationCreateValidator;
import com.together.server.infra.web.FcmService;
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

    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationCreateValidator notificationCreateValidator;
    private final FcmService fcmService;

    @Transactional
    public void createNotification(NotificationCreateRequest request) {
        notificationCreateValidator.validate(request);
        Notification notification = new Notification(request.title(), request.summary(), request.issue(), request.impact(), request.solution(), request.tags());
        notificationRepository.save(notification);

        List<Member> membersWithToken = memberRepository.findByFcmTokenIsNotNull();
        for (Member member : membersWithToken) {
            fcmService.sendNotification(
                    member.getFcmToken(),
                    request.title(),
                    request.summary()
            );
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationSimpleResponse> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(NotificationSimpleResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotificationDetailResponse getNotificationDetail(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOTIFICATION_NOT_FOUND));

        return NotificationDetailResponse.from(n);
    }
}
