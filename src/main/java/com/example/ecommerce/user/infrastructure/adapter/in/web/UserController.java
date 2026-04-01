package com.example.ecommerce.user.infrastructure.adapter.in.web;

import com.example.ecommerce.user.application.port.in.AuthenticateUserUseCase;
import com.example.ecommerce.user.application.port.in.RegisterUserUseCase;
import com.example.ecommerce.user.application.service.JWTService;
import com.example.ecommerce.user.domain.model.User;
import com.example.ecommerce.user.infrastructure.adapter.in.web.dto.AuthResponse;
import com.example.ecommerce.user.infrastructure.adapter.in.web.dto.LoginRequest;
import com.example.ecommerce.user.infrastructure.adapter.in.web.dto.RefreshTokenRequest;
import com.example.ecommerce.user.infrastructure.adapter.in.web.dto.UserProfileResponse;
import com.example.ecommerce.user.infrastructure.adapter.in.web.dto.UserRegistrationRequest;
import com.example.ecommerce.user.infrastructure.adapter.in.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/users", "/api/auth", "/auth"})
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final JWTService jwtService;

    public UserController(RegisterUserUseCase registerUserUseCase, 
                         AuthenticateUserUseCase authenticateUserUseCase,
                         JWTService jwtService) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        User user = registerUserUseCase.register(request.getEmail(), request.getPassword());
        UserResponse response = new UserResponse(user.getId(), user.getEmail());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticateUserUseCase.TokenPair tokenPair = authenticateUserUseCase.authenticate(
                request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new AuthResponse(
                tokenPair.accessToken(), 
                tokenPair.refreshToken(), 
                tokenPair.expiresIn()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String newAccessToken = jwtService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(new AuthResponse(newAccessToken, request.getRefreshToken(), 86400000));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        String email = principal.getEmail();
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        UserProfileResponse response = new UserProfileResponse(
                principal.getUserId(),
                username,
                email,
                "USER"
        );
        return ResponseEntity.ok(response);
    }
}
