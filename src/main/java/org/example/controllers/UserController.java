package org.example.controllers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.dto.AckDto;
import org.example.dto.UsersDto;
import org.example.controllers.helpers.ControllerHelper;
import org.example.exceptions.BadRequestException;
import org.example.exceptions.NotFoundException;
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
    public static final String REGISTRATION_USER = "/api/users/registration";
    public static final String AUTHORIZATION_USER = "/api/users/authorization";
    public static final String DELETE_USER = "/api/users/{id}";

    @GetMapping(GET_USERS)
    public List<UsersDto> getAllUsers () {
        List<UserEntity> users = userRepository.findAll();
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
        final UserEntity createUser = userGetId
                .map(userHelper::getProjectOrThrowException)
                .orElseGet(() -> UserEntity.builder().build());

        userGetName.ifPresent(userName -> {
            userRepository
                    .findByName(userName)
                    .filter(anotherUser -> !Objects.equals(anotherUser.getId(), createUser.getId()))
                    .ifPresent(anotherUser -> {
                        throw new BadRequestException ("Пользователь с таким именем уже существует");
                    });
            createUser.setName(userName);
        });
        userGetEmail.ifPresent(userEmail -> {
            userRepository
                    .findByEmail(userEmail)
                    .filter(anotherUser -> !Objects.equals(anotherUser.getId(), createUser.getId()))
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
    public AckDto authorization(
            @RequestParam (value = "name", required = false) String userName,
            @RequestParam (value = "email", required = false) String userEmail,
            @RequestParam (value = "password") String userPassword )
    {
        UserEntity user = null;
        if (userName != null && userEmail == null) {
           user = userRepository.findTopByNameAndPassword(userName, userPassword)
                    .orElse(null);
        }
        if (userEmail != null && userName == null) {
           user = userRepository.findTopByEmailAndPassword(userEmail, userPassword)
                    .orElse(null);
        }
        if (user == null) {
            throw new NotFoundException("Пользователя не существует");
        }

        return AckDto.makeDefault(true);
// код костыльный. Необходимо доработать
//Пользователь может авторизовываться как по имени, так и по почте отдельно.
//Решил, что пусть фронтендер будет получать true. Но можно сделать так, чтобы возвращались все данные о юзере
    }
    @DeleteMapping(DELETE_USER)
    public AckDto deleteUser (@RequestParam (value = "id") Long id) {
        userHelper.getProjectOrThrowException(id);
        userRepository.deleteById(id);
        return AckDto.makeDefault(true);
    }
}
