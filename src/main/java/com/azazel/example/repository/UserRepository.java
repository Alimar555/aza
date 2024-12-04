package com.azazel.example.repository;

import java.util.Optional;
import java.util.UUID;

import com.azazel.example.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findById(UUID id);
  Optional<User> findByEmail(String email);
}

