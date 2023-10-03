package org.example.controllers.helpers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.exceptions.NotFoundException;
import org.example.store.Repositories.UserRepository;
import org.example.store.entities.UserEntity;
import org.springframework.stereotype.Component;
@RequiredArgsConstructor
@Component
@Transactional
@FieldDefaults (level = AccessLevel.PRIVATE, makeFinal = true)
public class ControllerHelper {

    UserRepository userRepository;
    public UserEntity getProjectOrThrowException (Long id) {
       return userRepository
               .findById(id)
               .orElseThrow(() ->
                       new NotFoundException
                               (String.format("User \"%s\" doesn't exists" , id))

                       );
    }
}
