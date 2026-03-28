package com.example.ecommerce.user.application.port.in;

import com.example.ecommerce.user.domain.model.User;

public interface RegisterUserUseCase {
    User register(String email, String rawPassword);
}