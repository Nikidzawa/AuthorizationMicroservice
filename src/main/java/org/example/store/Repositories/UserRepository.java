package org.example.store.Repositories;

import org.example.store.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long> {
    Optional<User> findByName (String name);
    Optional<User> findByEmail (String email);
}
