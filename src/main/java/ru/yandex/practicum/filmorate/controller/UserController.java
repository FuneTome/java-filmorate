package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import java.util.logging.Level;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @GetMapping
    public Collection<User> getFilms() {
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        if (!isValid(user)) {
            logger.log(Level.WARNING, "Валидация пользователя не пройдена");
            throw new ValidationException("Валидация пользователя не пройдена");
        }
        user.setId(getNextId());
        if (user.getName() == null) { user.setName(user.getLogin()); }
        users.put(user.getId(), user);
        logger.log(Level.INFO, "Пользователь добавлен: " + user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (isValid(newUser)) {
                oldUser.setEmail(newUser.getEmail());
                oldUser.setLogin(newUser.getLogin());
                oldUser.setName(newUser.getName());
                oldUser.setBirthday(newUser.getBirthday());
                logger.log(Level.INFO, "Пользователь обновлен: " + newUser.getId());
                return oldUser;
            } else {
                logger.log(Level.WARNING, "Валидация пользователя при обновлении не пройдена");
                return null;
            }
        }
        throw new NotFoundException("Юзер с id = " + newUser.getId() + " не найден");
    }

    public boolean isValid(User user) {
        try {
            if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
            } else if (user.getLogin() == null || user.getLogin().isBlank()) {
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            } else if (user.getBirthday().isAfter(LocalDate.now()) || user.getBirthday() == null) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            } else {
                return true;
            }
        } catch (ValidationException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
