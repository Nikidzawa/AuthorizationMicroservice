package org.example.store.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_entity")
public class User {
    @GeneratedValue (strategy = GenerationType.SEQUENCE)
    @Id
    Long id;

    @Column (name = "name", unique = true)
    String name;

    @Column (name = "email", unique = true)
    String email;

    @Column (name = "password")
    String password;
}
