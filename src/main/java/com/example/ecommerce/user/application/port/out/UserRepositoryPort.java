package com.example.ecommerce.user.application.port.out;

import com.example.ecommerce.user.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    List<User> findAll();
    Optional<User> findByResetToken(String resetToken);
}