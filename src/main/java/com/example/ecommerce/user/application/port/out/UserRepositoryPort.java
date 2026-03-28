package com.example.ecommerce.user.application.port.out;

import com.example.ecommerce.user.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(String email);
}