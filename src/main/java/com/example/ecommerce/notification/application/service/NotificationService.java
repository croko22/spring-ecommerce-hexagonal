package com.example.ecommerce.notification.application.service;

import com.example.ecommerce.notification.application.port.in.SendNotificationUseCase;
import com.example.ecommerce.notification.application.port.out.EmailSenderPort;
import com.example.ecommerce.notification.application.port.out.NotificationRepositoryPort;
import com.example.ecommerce.notification.domain.model.Notification;
import com.example.ecommerce.notification.domain.model.NotificationType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;

public class NotificationService implements SendNotificationUseCase {

    private final EmailSenderPort emailSenderPort;
    private final NotificationRepositoryPort notificationRepositoryPort;

    @Value("${app.email.from}")
    private String fromEmail;

    public NotificationService(EmailSenderPort emailSenderPort,
                               NotificationRepositoryPort notificationRepositoryPort) {
        this.emailSenderPort = emailSenderPort;
        this.notificationRepositoryPort = notificationRepositoryPort;
    }

    @Override
    public Notification sendOrderConfirmation(Long userId, String recipientEmail, String orderNumber, double totalAmount) {
        String subject = "Order Confirmation - " + orderNumber;
        String body = buildOrderConfirmationBody(orderNumber, totalAmount);
        return sendNotification(userId, recipientEmail, NotificationType.ORDER_CONFIRMATION, subject, body);
    }

    @Override
    public Notification sendOrderShipped(Long userId, String recipientEmail, String orderNumber) {
        String subject = "Your Order Has Been Shipped - " + orderNumber;
        String body = buildOrderShippedBody(orderNumber);
        return sendNotification(userId, recipientEmail, NotificationType.ORDER_SHIPPED, subject, body);
    }

    @Override
    public Notification sendWelcome(Long userId, String recipientEmail, String userName) {
        String subject = "Welcome to Ecommerce!";
        String body = buildWelcomeBody(userName);
        return sendNotification(userId, recipientEmail, NotificationType.WELCOME, subject, body);
    }

    @Override
    public Notification sendPasswordReset(Long userId, String recipientEmail, String resetToken) {
        String subject = "Password Reset Request";
        String body = buildPasswordResetBody(resetToken);
        return sendNotification(userId, recipientEmail, NotificationType.PASSWORD_RESET, subject, body);
    }

    @Override
    @Retry(name = "backend")
    @CircuitBreaker(name = "backend")
    public Notification sendNotification(Long userId, String recipientEmail, NotificationType type,
                                         String subject, String body) {
        Notification notification = new Notification(
                null,
                userId,
                recipientEmail,
                type,
                subject,
                body,
                false,
                null,
                null
        );

        Notification saved = notificationRepositoryPort.save(notification);

        try {
            emailSenderPort.sendEmail(recipientEmail, subject, body);
            saved.markAsSent();
            return notificationRepositoryPort.save(saved);
        } catch (Exception e) {
            return saved;
        }
    }

    private String buildOrderConfirmationBody(String orderNumber, double totalAmount) {
        return "<html>"
                + "<body>"
                + "<h1>Order Confirmation</h1>"
                + "<p>Thank you for your order!</p>"
                + "<p><strong>Order Number:</strong> " + orderNumber + "</p>"
                + "<p><strong>Total Amount:</strong> $" + String.format("%.2f", totalAmount) + "</p>"
                + "<p>We will notify you when your order is shipped.</p>"
                + "</body>"
                + "</html>";
    }

    private String buildOrderShippedBody(String orderNumber) {
        return "<html>"
                + "<body>"
                + "<h1>Order Shipped</h1>"
                + "<p>Good news! Your order has been shipped.</p>"
                + "<p><strong>Order Number:</strong> " + orderNumber + "</p>"
                + "<p>You will receive your package soon.</p>"
                + "</body>"
                + "</html>";
    }

    private String buildWelcomeBody(String userName) {
        return "<html>"
                + "<body>"
                + "<h1>Welcome to Ecommerce!</h1>"
                + "<p>Hi " + userName + ",</p>"
                + "<p>Thank you for joining us. We are excited to have you on board.</p>"
                + "<p>Start exploring our products and enjoy shopping!</p>"
                + "</body>"
                + "</html>";
    }

    private String buildPasswordResetBody(String resetToken) {
        return "<html>"
                + "<body>"
                + "<h1>Password Reset</h1>"
                + "<p>You requested a password reset.</p>"
                + "<p><strong>Reset Token:</strong> " + resetToken + "</p>"
                + "<p>Use this token to reset your password. It will expire in 1 hour.</p>"
                + "<p>If you did not request this, please ignore this email.</p>"
                + "</body>"
                + "</html>";
    }
}