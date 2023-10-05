package org.example.factory;

import org.example.dto.UsersDto;
import org.example.store.entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserDtoFactory {
    public UsersDto makeUserFactory (UserEntity user) {
        return UsersDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .build();
    }
}
