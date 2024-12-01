package com.alibou.security.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

//public interface UserRepository extends JpaRepository<User, Integer> {
//
//  Optional<User> findByEmail(String email);
//
//}

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findById(UUID id);
  Optional<User> findByEmail(String email);
}

