package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User oldUser, User newUser);

    boolean findById(Long id);

    User getById(Long id);

    Map<Long, User> getUsers();
}
