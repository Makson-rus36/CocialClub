package com.rest.cocial.repo;

import com.rest.cocial.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailsRepo extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
}
