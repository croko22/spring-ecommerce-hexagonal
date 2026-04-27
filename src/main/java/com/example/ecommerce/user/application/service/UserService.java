package com.example.ecommerce.user.application.service;

import com.example.ecommerce.notification.application.port.in.SendNotificationUseCase;
import com.example.ecommerce.user.application.port.in.AuthenticateUserUseCase;
import com.example.ecommerce.user.application.port.in.RegisterUserUseCase;
import com.example.ecommerce.user.application.port.in.RequestPasswordResetUseCase;
import com.example.ecommerce.user.application.port.in.ResetPasswordUseCase;
import com.example.ecommerce.user.application.port.out.JWTProviderPort;
import com.example.ecommerce.user.application.port.out.PasswordEncoderPort;
import com.example.ecommerce.user.application.port.out.UserRepositoryPort;
import com.example.ecommerce.user.domain.exception.AuthenticationException;
import com.example.ecommerce.user.domain.exception.InvalidUserException;
import com.example.ecommerce.user.domain.exception.UserAlreadyExistsException;
import com.example.ecommerce.user.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements RegisterUserUseCase, AuthenticateUserUseCase,
        RequestPasswordResetUseCase, ResetPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final JWTProviderPort jwtProviderPort;
    private final SendNotificationUseCase sendNotificationUseCase;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordEncoderPort passwordEncoderPort,
                       JWTProviderPort jwtProviderPort, SendNotificationUseCase sendNotificationUseCase) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.jwtProviderPort = jwtProviderPort;
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    @Override
    public User register(String email, String rawPassword) {
        if (userRepositoryPort.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        String encodedPassword = passwordEncoderPort.encode(rawPassword);
        User user = new User(null, email, encodedPassword);

        User savedUser = userRepositoryPort.save(user);

        String userName = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        sendNotificationUseCase.sendWelcome(savedUser.getId(), savedUser.getEmail(), userName);

        return savedUser;
    }

    @Override
    public TokenPair authenticate(String email, String rawPassword) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoderPort.matches(rawPassword, user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        String accessToken = jwtProviderPort.generateToken(email, user.getId(), user.getRole().name());
        String refreshToken = jwtProviderPort.generateRefreshToken(email, user.getId(), user.getRole().name());

        return new TokenPair(accessToken, refreshToken, jwtExpiration);
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepositoryPort.findByEmail(email)
                .orElse(null);

        if (user == null) {
            // Do not reveal if email exists for security
            return;
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        user.setResetToken(token);
        user.setResetTokenExpiry(expiry);
        userRepositoryPort.save(user);

        sendNotificationUseCase.sendPasswordReset(user.getId(), user.getEmail(), token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 6) {
            throw new InvalidUserException("Password must be at least 6 characters long");
        }

        User user = userRepositoryPort.findByResetToken(token)
                .orElseThrow(() -> new InvalidUserException("Invalid or expired reset token"));

        if (!user.isResetTokenValid(token)) {
            throw new InvalidUserException("Invalid or expired reset token");
        }

        String encodedPassword = passwordEncoderPort.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepositoryPort.save(user);
    }
}