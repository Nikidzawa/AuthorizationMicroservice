package org.example.store.Repositories;

import org.example.store.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository <UserEntity, Long> {
    Optional<UserEntity> findByName (String name);
    Optional<UserEntity> findByEmail (String email);
    Optional<UserEntity> findTopByNameAndPassword (String login, String password);
    Optional<UserEntity> findTopByEmailAndPassword (String email, String password);
}
