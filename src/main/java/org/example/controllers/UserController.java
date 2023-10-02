package org.example.controllers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.Dto.AckDto;
import org.example.Dto.UsersDto;
import org.example.controllers.helpers.UserHelper;
import org.example.exceptions.BadRequestException;
import org.example.factory.UserDtoFactory;
import org.example.store.Repositories.UserRepository;
import org.example.store.entities.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class UserController {

   UserRepository userRepository;
   UserDtoFactory userDtoFactory;
   UserHelper userHelper;

   public  static final String GET_USERS = "/api/users";
    public static final String CREATE_OR_EDIT_USER = "/api/users";
    public static final String DELETE_USER = "/api/users/{id}";

    @GetMapping(GET_USERS)
    public List<UsersDto> getAllUsers () {
        List<User> users = userRepository.findAll();
        return  users.stream()
                .map(userDtoFactory::makeUserFactory)
                .collect(Collectors.toList());
    }

    @PutMapping(CREATE_OR_EDIT_USER)
    public UsersDto createOrUpdateUser (
            @RequestParam (value = "id", required = false) Optional <Long> userGetId,
            @RequestParam (value = "name") Optional <String> userGetName,
            @RequestParam (value = "email") Optional <String> userGetEmail,
            @RequestParam (value = "password") String userPassword )
    {
        userGetEmail = userGetEmail.filter(userEmail -> !userEmail.trim().isEmpty());
        userGetName = userGetName.filter(userName -> !userName.trim().isEmpty());
        boolean isCreate = userGetId.isEmpty();

        if (userGetEmail.isEmpty() && userGetName.isEmpty() && isCreate) {
            throw new BadRequestException("User can't be empty");
        }

        final User updateUser = userGetId
                .map(userHelper::getProjectOrThrowException)
                .orElseGet(() -> User.builder().build());

        userGetName.ifPresent(userName -> {
            userRepository
                    .findByName(userName)
                    .filter(anotherUser -> !Objects.equals(anotherUser.getId(), updateUser.getId()))
                    .ifPresent(anotherUser -> {
                        throw new BadRequestException (
                                String.format("User \"%s\" already exists", userName)
                        );
                    });
            updateUser.setName(userName);
        });
        userGetEmail.ifPresent(userEmail -> {
            userRepository
                    .findByEmail(userEmail)
                    .filter(anotherUser -> !Objects.equals(anotherUser.getId(), updateUser.getId()))
                    .ifPresent(anotherUser -> {
                        throw new BadRequestException(
                                String.format("User \"%s\" already exists", userEmail)
                        );
                    });
            updateUser.setEmail(userEmail);
        });

        updateUser.setPassword(userPassword);

        final User updatedUser = userRepository.saveAndFlush(updateUser);

        return userDtoFactory.makeUserFactory(updatedUser);
    }

    @DeleteMapping(DELETE_USER)
    public AckDto deleteUser (@RequestParam (value = "id") Long id) {
        userHelper.getProjectOrThrowException(id);
        userRepository.deleteById(id);
        return AckDto.makeDefault(true);
    }
}
