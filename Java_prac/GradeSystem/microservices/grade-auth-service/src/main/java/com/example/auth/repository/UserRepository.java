package com.example.auth.repository;

import com.example.auth.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

    List<User> findByEnabled(boolean enabled);

    List<User> findByEnabledTrue();

    List<User> findByEnabledFalse();

    Optional<User> findByUsernameAndEnabled(String username, boolean enabled);

    boolean existsByUsername(String username);

    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countEnabledUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = false")
    long countDisabledUsers();

    @Query("SELECT u FROM User u ORDER BY u.username ASC")
    List<User> findAllOrderByUsernameAsc();
}
