package com.diceroller.repository;

import com.diceroller.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByResetToken(String resetToken);
    Optional<User> findByEmail(String email);
    User findFirstByName(String name);
}
