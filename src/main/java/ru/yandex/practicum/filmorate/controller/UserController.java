package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private Long id = 1L;

    @GetMapping
    public Collection<User> getFilms() {
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        if (!isValid(user)) {
            log.warn("Валидация пользователя не пройдена");
            throw new ValidationException("Валидация пользователя не пройдена");
        }
        user.setId(id);
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        users.put(id++, user);
        log.info("Пользователь добавлен: " + user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!users.containsKey(newUser.getId())) {
            throw new NotFoundException("Юзер с id = " + newUser.getId() + " не найден");
        } else {
            User oldUser = users.get(newUser.getId());
            if (isValid(newUser)) {
                oldUser.setEmail(newUser.getEmail());
                oldUser.setLogin(newUser.getLogin());
                oldUser.setName(newUser.getName());
                oldUser.setBirthday(newUser.getBirthday());
                log.info("Пользователь обновлен: " + newUser.getId());
                return oldUser;
            } else {
                log.warn("Валидация пользователя при обновлении не пройдена");
                return null;
            }
        }
    }

    public boolean isValid(User user) {
        try {
            if (user.getBirthday().isAfter(LocalDate.now()) || user.getBirthday() == null) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            } else {
                return true;
            }
        } catch (ValidationException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }
}
