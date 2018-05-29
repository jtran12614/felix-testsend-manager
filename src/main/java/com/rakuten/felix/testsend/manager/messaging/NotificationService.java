package com.rakuten.felix.testsend.manager.messaging;

import com.rakuten.felix.testsend.manager.messaging.dto.Notification;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Slf4j
@Service
public class NotificationService {
    private final String notificationUrl;
    private final String notificationSuccessTitle;
    private final String notificationSuccessMessage;
    private final String notificationErrorTitle;
    private final String notificationErrorMessage;
    private final MessageSender messageSender;
    private static final Pattern BUNDLE_ID_PATTERN = Pattern.compile(Pattern.quote("{bundle_id}"));

    /**
     * Constructor.
     *
     * @param notificationUrl            Notification url.
     * @param notificationSuccessTitle   Notification Title for Success Case
     * @param notificationSuccessMessage Notification Message for Success Case
     * @param notificationErrorTitle     Notification Title for Fail Case
     * @param notificationErrorMessage   Notification Message for Fail Case
     * @param messageSender              Message sender.
     */
    @Autowired
    public NotificationService(@Value("${com.rakuten.felix.testsend-manager.notification.url}") String notificationUrl,
                               @Value("${com.rakuten.felix.testsend-manager.notification.success.title}") String notificationSuccessTitle,
                               @Value("${com.rakuten.felix.testsend-manager.notification.success.message}") String notificationSuccessMessage,
                               @Value("${com.rakuten.felix.testsend-manager.notification.error.title}") String notificationErrorTitle,
                               @Value("${com.rakuten.felix.testsend-manager.notification.error.message}") String notificationErrorMessage,
                               MessageSender messageSender) {

        this.notificationUrl = notificationUrl;
        this.notificationSuccessTitle = notificationSuccessTitle;
        this.notificationSuccessMessage = notificationSuccessMessage;
        this.notificationErrorTitle = notificationErrorTitle;
        this.notificationErrorMessage = notificationErrorMessage;
        this.messageSender = messageSender;
    }


    /**
     * Publishes test send success message to notification channel.
     *
     * @param bundleId Bundle id.
     * @param userId   User id.
     */
    public void publishSuccessNotification(Integer bundleId, Integer userId) {
        val notification = buildNotification(notificationSuccessTitle, notificationSuccessMessage, bundleId, userId);
        messageSender.publishNotification(notification);
    }

    /**
     * Publishes test send error message to notification channel.
     *
     * @param bundleId Bundle id.
     * @param userId   User id.
     */
    public void publishErrorNotification(Integer bundleId, Integer userId) {
        val notification = buildNotification(notificationErrorTitle, notificationErrorMessage, bundleId, userId);
        messageSender.publishNotification(notification);
    }

    private Notification buildNotification(String title, String message, Integer bundleId, Integer userId) {
        val replacedUrl = BUNDLE_ID_PATTERN.matcher(notificationUrl).replaceAll(bundleId.toString());
        val replacedTitle = BUNDLE_ID_PATTERN.matcher(title).replaceAll(bundleId.toString());
        val replacedMessage = BUNDLE_ID_PATTERN.matcher(message).replaceAll(bundleId.toString());

        return Notification.builder()
                           .userId(userId.longValue())
                           .url(replacedUrl)
                           .noticeFlag(true)
                           .title(replacedTitle)
                           .message(replacedMessage)
                           .build();
    }

}
