package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long id = 1L;

    @Override
    public User addUser(User user) {
        user.setId(id);
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        users.put(id++, user);
        log.info("Пользователь добавлен: " + user.getId());
        return user;
    }

    @Override
    public User updateUser(User oldUser, User newUser) {
        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setName(newUser.getName());
        oldUser.setBirthday(newUser.getBirthday());
        log.info("Пользователь обновлен: " + newUser.getId());
        return newUser;
    }
}
