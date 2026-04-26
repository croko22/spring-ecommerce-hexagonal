package com.example.ecommerce.user.infrastructure.adapter.out.persistence;

import com.example.ecommerce.user.application.port.out.UserRepositoryPort;
import com.example.ecommerce.user.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity(user.getId(), user.getEmail(), user.getPassword(), user.getRole());
        UserEntity savedEntity = jpaUserRepository.save(entity);
        return new User(savedEntity.getId(), savedEntity.getEmail(), savedEntity.getPassword(), savedEntity.getRole());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
                .map(entity -> new User(entity.getId(), entity.getEmail(), entity.getPassword(), entity.getRole()));
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id)
                .map(entity -> new User(entity.getId(), entity.getEmail(), entity.getPassword(), entity.getRole()));
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll().stream()
                .map(entity -> new User(entity.getId(), entity.getEmail(), entity.getPassword(), entity.getRole()))
                .toList();
    }
}