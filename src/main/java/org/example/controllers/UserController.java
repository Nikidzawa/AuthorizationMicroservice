package org.example.controllers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.dto.AckDto;
import org.example.dto.UsersDto;
import org.example.controllers.helpers.ControllerHelper;
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
   ControllerHelper userHelper;

   public  static final String GET_USERS = "/api/users";
    public static final String REGISTRATION_USER = "/api/users";


    public static final String DELETE_USER = "/api/users";

    @GetMapping(GET_USERS)
    public List<UsersDto> getAllUsers () {
        List<User> users = userRepository.findAll();
        return  users.stream()
                .map(userDtoFactory::makeUserFactory)
                .collect(Collectors.toList());
    }

    @PostMapping(REGISTRATION_USER)
    public UsersDto createUser (
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

        final User createUser = userGetId
                .map(userHelper::getProjectOrThrowException)
                .orElseGet(() -> User.builder().build());

        userGetName.ifPresent(userName -> {
            userRepository
                    .findByName(userName)
                    .filter(anotherUser -> !Objects.equals(anotherUser.getId(), createUser.getId()))
                    .ifPresent(anotherUser -> {
                        throw new BadRequestException (
                                String.format("User %s already exists", userName)
                        );
                    });
            createUser.setName(userName);
        });
        userGetEmail.ifPresent(userEmail -> {
            userRepository
                    .findByEmail(userEmail)
                    .filter(anotherUser -> !Objects.equals(anotherUser.getId(), createUser.getId()))
                    .ifPresent(anotherUser -> {
                        throw new BadRequestException(
                                String.format("User %s already exists", userEmail)
                        );
                    });
            createUser.setEmail(userEmail);
        });

        createUser.setPassword(userPassword);

        final User createdUser = userRepository.saveAndFlush(createUser);

        return userDtoFactory.makeUserFactory(createdUser);
    }

    @DeleteMapping(DELETE_USER)
    public AckDto deleteUser (@RequestParam (value = "id") Long id) {
        userHelper.getProjectOrThrowException(id);
        userRepository.deleteById(id);
        return AckDto.makeDefault(true);
    }
}
