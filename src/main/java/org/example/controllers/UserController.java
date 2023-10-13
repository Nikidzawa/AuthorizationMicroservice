package org.example.controllers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.dto.BooleanDto;
import org.example.dto.UsersDto;
import org.example.controllers.helpers.ControllerHelper;
import org.example.exceptions.BadRequestException;
import org.example.factory.UserDtoFactory;
import org.example.store.Repositories.UserRepository;
import org.example.store.entities.UserEntity;
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

    public static final String GET_USERS = "/api/users";
    public static final String GET_USER = "/api/users/{id}";
    public static final String REGISTRATION_USER = "/api/users/registration";
    public static final String AUTHORIZATION_USER = "/api/users/authorization";
    public static final String EDIT_USER = "/api/users/edit/{id}";
    public static final String DELETE_USER = "/api/users/{id}";

    @GetMapping(GET_USERS)
    public List<UsersDto> getAllUsers () {
        List<UserEntity> users = userRepository.findAll();
        return  users.stream()
                .map(userDtoFactory::makeUserFactory)
                .collect(Collectors.toList());
    }
    @GetMapping(GET_USER)
    public UserEntity getUser (@PathVariable (value = "id") Long id )
    {
        return userHelper.getProjectOrThrowException(id);
    }
    @PostMapping(REGISTRATION_USER)
    public UsersDto createUser (
            @RequestParam (value = "id", required = false) Optional <Long> userGetId,
            @RequestParam (value = "name") Optional <String> userGetName,
            @RequestParam (value = "email") Optional <String> userGetEmail,
            @RequestParam (value = "password") String userPassword )
    {
        final UserEntity createUser = userGetId
                .map(userHelper::getProjectOrThrowException)
                .orElseGet(() -> UserEntity.builder().build());

        userGetName.ifPresent(userName -> {
            userRepository
                    .findByName(userName)
                    .ifPresent(anotherUser -> {
                        throw new BadRequestException ("Пользователь с таким именем уже существует");
                    });
            createUser.setName(userName);
        });
        userGetEmail.ifPresent(userEmail -> {
            userRepository
                    .findByEmail(userEmail)
                    .ifPresent(anotherUser -> {
                        throw new BadRequestException("Пользователь с такой почтой уже существует");
                    });
            createUser.setEmail(userEmail); //тут можно запихнуть шифрование
        });

        createUser.setPassword(userPassword); //тут можно запихнуть шифрование

        final UserEntity createdUser = userRepository.saveAndFlush(createUser);

        return userDtoFactory.makeUserFactory(createdUser);
    }

    @GetMapping(AUTHORIZATION_USER)
    public BooleanDto authorization(
            @RequestParam (value = "email") String userEmail,
            @RequestParam (value = "password") String userPassword )
    {
      userRepository.findTopByEmailAndPassword(userEmail, userPassword)
              .orElseThrow(() -> new BadRequestException("Пользователь не найден"));

        return BooleanDto.makeDefault(true);
    }

    @PatchMapping (EDIT_USER)
    public UsersDto EditUser (
            @PathVariable ("id") Long id,
            @RequestParam ("name") Optional <String> name,
            @RequestParam ("email") Optional <String> email,
            @RequestParam ("password") Optional <String> password)
    {

        final UserEntity selectedUser = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Пользователя не существует"));

        name.ifPresent(userName -> {
            userRepository.findByName(userName)
                    .ifPresent(anotherUser -> {
                        throw new BadRequestException("Пользователь с таким именем уже существует");
                    });
            selectedUser.setName(userName);
        });

        email.ifPresent(userEmail -> {
            userRepository.findByEmail(userEmail)
                .ifPresent(anotherUser -> {
                    throw new BadRequestException("Пользователь с такой почтой уже существует");
                });
        selectedUser.setEmail(userEmail);
        });

        password.ifPresent(userPassword -> { userRepository.findByPassword(userPassword)
                .filter(anotherUser -> Objects.equals(anotherUser.getId(), selectedUser.getId()))
                .ifPresent(anotherUser -> {throw new BadRequestException("Пароли совпадают");
                });
            selectedUser.setPassword(userPassword);
                });

        userRepository.saveAndFlush(selectedUser);
        return userDtoFactory.makeUserFactory(selectedUser);
    }

    @DeleteMapping(DELETE_USER)
    public BooleanDto deleteUser (@PathVariable ("id") Long id) {
        userHelper.getProjectOrThrowException(id);
        userRepository.deleteById(id);
        return BooleanDto.makeDefault(true);
    }
}
