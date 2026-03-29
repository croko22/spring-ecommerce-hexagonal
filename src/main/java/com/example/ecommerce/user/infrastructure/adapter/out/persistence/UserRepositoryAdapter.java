package com.example.ecommerce.user.infrastructure.adapter.out.persistence;

import com.example.ecommerce.user.application.port.out.UserRepositoryPort;
import com.example.ecommerce.user.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity(user.getId(), user.getEmail(), user.getPassword());
        UserEntity savedEntity = jpaUserRepository.save(entity);
        return new User(savedEntity.getId(), savedEntity.getEmail(), savedEntity.getPassword());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
                .map(entity -> new User(entity.getId(), entity.getEmail(), entity.getPassword()));
    }
}