package com.example.ecommerce.user.application.service;

import com.example.ecommerce.user.application.port.in.AuthenticateUserUseCase;
import com.example.ecommerce.user.application.port.in.RegisterUserUseCase;
import com.example.ecommerce.user.application.port.out.PasswordEncoderPort;
import com.example.ecommerce.user.application.port.out.UserRepositoryPort;
import com.example.ecommerce.user.domain.exception.AuthenticationException;
import com.example.ecommerce.user.domain.exception.UserAlreadyExistsException;
import com.example.ecommerce.user.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService implements RegisterUserUseCase, AuthenticateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordEncoderPort passwordEncoderPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public User register(String email, String rawPassword) {
        if (userRepositoryPort.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }
        
        String encodedPassword = passwordEncoderPort.encode(rawPassword);
        User user = new User(null, email, encodedPassword);
        
        return userRepositoryPort.save(user);
    }

    @Override
    public String authenticate(String email, String rawPassword) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));
                
        if (!passwordEncoderPort.matches(rawPassword, user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }
        
        // Return a dummy token for simplicity
        return UUID.randomUUID().toString();
    }
}