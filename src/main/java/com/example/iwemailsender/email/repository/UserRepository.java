package com.example.iwemailsender.email.repository;


import com.example.iwemailsender.email.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    List<User> findByEnabledTrue();

    boolean existsByUsername(String username);
}
